using System.CommandLine;
using System.Diagnostics;
internal class Program
{

    static readonly string currentUserGUID = Guid.NewGuid().ToString();
    static readonly string currentLovelyVersion = "0.8.0";
    const int BalatroAppID = 2379780;
    private static int Main(string[] args)
    {
        var rootCommand = new RootCommand("Tool to build balatro APK for server to distribute");
        Option<string> buildDir = new("--buildDir", "-bd")
        {
            HelpName = "BuildDir",
            Description = "Directory which the tool builds the balatro apk for the current user",
            DefaultValueFactory = _ => Path.GetTempPath()
        };

        Option<string> balatroBinPath = new("--balatroBinpath", "-bbp")
        {
            HelpName = "BalatroBinPath",
            Description = "Directory where the balatro binary is found",
            DefaultValueFactory = _ =>
            {
                return @"/home/mohammad/.local/share/Steam/steamapps/common/Balatro";
            }
        };


        Option<string> balatroSavePath = new("--balatroSavepath", "-bsp")
        {
            HelpName = "BalatroSavePath",
            Description = "Directory where balatro saves are found",
            DefaultValueFactory = _ =>
            {
                return @"/home/mohammad/.steam/steam/steamapps/compatdata/2379780/pfx/drive_c/users/steamuser/AppData/Roaming/Balatro";
            }
        };
        Option<string> bmmPath = new("--bmmpath", "-bmmp")
        {
            HelpName = "BMMPath",
            Description = "Directory where BMM is found",
            DefaultValueFactory = _ =>
            {
                return @"/home/mohammad/Downloads/BMM/balatro-mobile-maker";
            }
        };

        foreach (var item in new Option[] { buildDir, balatroBinPath, bmmPath, balatroSavePath })
        {
            // we want to set them to not required to use the default factory values
            item.Required = false;
            // ensure all paths are actually valid
            item.Validators.Add(result =>
            {
                var referencedPath = result.GetValueOrDefault<string>();
                if (!(Path.Exists(referencedPath) || string.IsNullOrEmpty(referencedPath)))
                {
                    result.AddError("Build Directory does not exist!");
                }

            });
            // then add the options
            rootCommand.Options.Add(item);
        }
        ParseResult result = rootCommand.Parse(args);
        try
        {
        if (result.Errors.Count == 0)
        {
            // start steam first to mount proton windows prefix
            var steamProcess = new Process();
            steamProcess.StartInfo.FileName = "steam";
            Process process = new();
            const string bmmOptions = "nyyyny\nnyyyy";
            var parsedBMMPath = result.GetRequiredValue(bmmPath);
            var parsedBalatroBinPath = result.GetRequiredValue(balatroBinPath);
            var parsedBalatroSavesPath = result.GetRequiredValue(balatroSavePath);
            BackupBalatroSaves(parsedBalatroSavesPath);
            // start balatro first with mods to refresh dump files
            StartBalatroWithTimeout();
            PrepareForSaveTransfer(parsedBalatroSavesPath);
            process.StartInfo.FileName = parsedBMMPath;
            process.StartInfo.UseShellExecute = false;
            process.StartInfo.RedirectStandardInput = true;
            process.Start();
            StreamWriter writer = process.StandardInput;
            foreach (var item in bmmOptions.ToCharArray())
            {
                writer.Write($"{item}\n");
            }
            process.WaitForExit();
            Console.WriteLine($"Built balatro at path {Path.Join(parsedBMMPath, "balatro.apk")} from {parsedBalatroBinPath} ");
            CleanupAfterBuild(parsedBalatroSavesPath);
            return 0;
        }
        else
        {
            foreach (var parseError in result.Errors)
            {
                Console.Error.WriteLine(parseError.Message);
            }
            return 1;
        }
        }
        catch (InvalidOperationException)
        {
            // BMM is crying that it can't control the STDIN. Too bad!
            return 0;
        }
    }


    private static void PrepareForSaveTransfer(string ParsedBalatroSavePath)
    {

        // before build with BMM, prepare copy for main machine
        var balatroSavePathInfo = new DirectoryInfo(ParsedBalatroSavePath) ?? throw new Exception($"{ParsedBalatroSavePath} is null or invalid! ");
        var pathParent = balatroSavePathInfo.Parent ?? throw new Exception($"{ParsedBalatroSavePath} is null or invalid! ");
        var newName = Path.Join(pathParent.FullName, $"{balatroSavePathInfo.Name} Copy {currentUserGUID}");
        var currentSavePath = balatroSavePathInfo.FullName;
        var modsDir = Path.Combine([currentSavePath, "Mods"]);
        // local util function
        string balatroSavePathCombine(params string[] paths)
        {
            return PathCombine(currentSavePath, paths);
        }
        string modsPathCombine(params string[] paths)
        {
            return PathCombine(modsDir, paths);
        }

        // assuming that we have a backup, work on main dir
        var lovelyDumpFiles = Path.Combine([modsDir, "lovely", "dump"]);
        // copy all the dump files into balatro dir
        CopyDirectory(lovelyDumpFiles, currentSavePath, true);
        // then copy relevant lua files into balatro dir
        File.Move(Path.Combine([modsDir, "Steamodded", "libs", "nativefs", "nativefs.lua"]), Path.Combine([currentSavePath, "nativefs.lua"]));
        File.Move(Path.Combine([modsDir, "Steamodded", "libs", "json", "json.lua"]), Path.Combine([currentSavePath, "json.lua"]));
        // create lovely.lua file with latest version
        var lovelyLuaPath = Path.Combine([currentSavePath, "lovely.lua"]);
        File.WriteAllText(lovelyLuaPath, $$"""
        return {
        repo = "<https://github.com/ethangreen-dev/lovely-injector>",
        version = "{{currentLovelyVersion}}",
        mod_dir = "/data/data/com.unofficial.balatro/files/save/game/Mods",
        }
        """);
        // create SMODS folder
        var smodsPath = Path.Combine([currentSavePath, "SMODS"]);
        Directory.CreateDirectory(smodsPath);
        // move release.lua and version.lua to smods folder in the root saves directory
        File.Move(Path.Combine([modsDir, "Steamodded", "version.lua"]), balatroSavePathCombine("SMODS", "version.lua"));
        File.Move(Path.Combine([modsDir, "Steamodded", "release.lua"]), balatroSavePathCombine("SMODS", "release.lua"));
        // check if we have talisman
        var isTalismanPresent = Directory.GetDirectories(modsDir).Any(x => x.Contains("talisman", StringComparison.CurrentCultureIgnoreCase));
        if (isTalismanPresent)
        {
            // perform talisman step
            var nativefsluaTalisman = modsPathCombine("Talisman", "nativefs.lua");
            var talismanNativeFsDir = balatroSavePathCombine("nativefs", "nativefs.lua");
            Directory.CreateDirectory(balatroSavePathCombine("nativefs"));
            File.Move(nativefsluaTalisman, talismanNativeFsDir);
        }
        // we should then be done

    }

    private static void StartBalatroWithTimeout(int timeoutSeconds = 35)
    {
        var balatroProcess = new Process();

        balatroProcess.StartInfo.FileName = "steam";
        balatroProcess.StartInfo.UseShellExecute = true;
        balatroProcess.StartInfo.Arguments = "-applaunch 2379780";
        balatroProcess.Start();
        Thread.Sleep(TimeSpan.FromSeconds(timeoutSeconds));
        // after we wait for timeout, kill balatro process only
        var extraprocesses = Process.GetProcessesByName("Balatro.exe");
        foreach (var p in extraprocesses)
        {
            p.Kill();
        }
    }

    private static void BackupBalatroSaves(string ParsedBalatroSavePath)
    {
        // before build with BMM, prepare copy for main machine
        var balatroSavePathInfo = new DirectoryInfo(ParsedBalatroSavePath) ?? throw new Exception($"{ParsedBalatroSavePath} is null or invalid! ");
        var pathParent = balatroSavePathInfo.Parent ?? throw new Exception($"{ParsedBalatroSavePath} is null or invalid! ");
        var newName = Path.Join(pathParent.FullName, $"{balatroSavePathInfo.Name} Copy {currentUserGUID}");
        var currentSavePath = balatroSavePathInfo.FullName;
        CopyDirectory(currentSavePath, newName, true);
    }

    private static string PathCombine(string firstPath, string[] paths)
    {
        return Path.Combine([firstPath, .. paths]);
    }
    private static void CleanupAfterBuild(string ParsedBalatroSavePath)
    {
        var balatroSavePathInfo = new DirectoryInfo(ParsedBalatroSavePath) ?? throw new Exception($"{ParsedBalatroSavePath} is null or invalid! ");
        var pathParent = balatroSavePathInfo.Parent ?? throw new Exception($"{ParsedBalatroSavePath} is null or invalid! ");
        Directory.Delete(ParsedBalatroSavePath,true);
        // rename backed up folder to Balatro to restore backup
        Directory.Move(Path.Join(pathParent.FullName, $"{balatroSavePathInfo.Name} Copy {currentUserGUID}"), ParsedBalatroSavePath);
    }

    // taken from MSDN
    private static void CopyDirectory(string sourceDir, string destinationDir, bool recursive)
    {
        // Get information about the source directory
        var dir = new DirectoryInfo(sourceDir);

        // Check if the source directory exists
        if (!dir.Exists)
            throw new DirectoryNotFoundException($"Source directory not found: {dir.FullName}");

        // Cache directories before we start copying
        DirectoryInfo[] dirs = dir.GetDirectories();

        // Create the destination directory
        Directory.CreateDirectory(destinationDir);

        // Get the files in the source directory and copy to the destination directory
        foreach (FileInfo file in dir.GetFiles())
        {
            string targetFilePath = Path.Combine(destinationDir, file.Name);
            file.CopyTo(targetFilePath);
        }

        // If recursive and copying subdirectories, recursively call this method
        if (recursive)
        {
            foreach (DirectoryInfo subDir in dirs)
            {
                string newDestinationDir = Path.Combine(destinationDir, subDir.Name);
                CopyDirectory(subDir.FullName, newDestinationDir, true);
            }
        }
    }

}