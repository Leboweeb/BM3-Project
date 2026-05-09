using System.CommandLine;
using System.Diagnostics;
using System.IO.Compression;
using System.Runtime.InteropServices;
using System.Runtime.Serialization;
internal class Program
{

    [Serializable()]
    internal class UnsupportedOSExecption : Exception, ISerializable
    {
        public UnsupportedOSExecption()
        {
        }

        public UnsupportedOSExecption(string? message) : base(message)
        {
        }

        public UnsupportedOSExecption(string? message, Exception? innerException) : base(message, innerException)
        {
        }

    }

    static readonly string defaultGUID = Guid.NewGuid().ToString();
    static readonly string currentLovelyVersion = "0.9.0";
    static readonly string currentDate = DateTime.Now.ToString("dd-MM-yyyy");

    static readonly int balatroAppID = 2379780;
    private static readonly string bm3SavesDirName = "BM3-Saves";

    private static int Main(string[] args)
    {
        var rootCommand = new RootCommand("Tool to build balatro APK for server to distribute");
        Option<string> saveName = new("--guid")
        {
            HelpName = "Guid",
            Description = "Provide guid to the build tool to use instead of it generating a custom one.",
            DefaultValueFactory = _ => $"{currentDate}-{defaultGUID}"
        };
        Option<string> buildDir = new("--buildDir", "-bd")
        {
            HelpName = "BuildDir",
            Description = "Directory which the tool builds the balatro apk for the current user",
            DefaultValueFactory = _ => CreateDirIfNotExists(Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.MyDocuments), bm3SavesDirName))
        };

        Option<int> timeOut = new("--timeout", "-t")
        {
            HelpName = "TimeoutValue",
            Description = "How much time (in seconds this script waits until balatro is fully loaded to exit. Default is 15 seconds)",
            DefaultValueFactory = _ =>
            {
                return 20;
            }
        };

        Option<string> balatroSavePath = new("--balatroSavepath", "-bsp")
        {
            HelpName = "BalatroSavePath",
            Description = "Directory where balatro saves are found",
            DefaultValueFactory = _ =>
            {
                return GetBalatroDefaultSavePath();
            }
        };
        Option<string> bmmPath = new("--bmmpath", "-bmmp")
        {
            HelpName = "BMMPath",
            Description = "Directory where BMM is found",
            DefaultValueFactory = _ =>
            {
                var userDownloadPath = Path.Combine(
                    Environment.GetFolderPath(Environment.SpecialFolder.UserProfile),
                    "Downloads"
                );
                return Path.Combine(userDownloadPath, "BMM");
            }
        };

        foreach (var item in new Option[] { buildDir, bmmPath, balatroSavePath })
        {
            // we want to set them to not required to use the default factory values
            item.Required = false;
            // ensure all paths are actually valid
            item.Validators.Add(result =>
            {
                var referencedPath = result.GetValueOrDefault<string>();
                if (!(Path.Exists(referencedPath) || string.IsNullOrEmpty(referencedPath)))
                {
                    result.AddError($"Path {referencedPath} does not exist!");
                }

            });
            // then add the options
            rootCommand.Options.Add(item);
        }
        timeOut.Validators.Add(result =>
        {
           var referencedValue = result.GetValueOrDefault<int>();
           if (referencedValue <= 0)
           {
                result.AddError("Timeout must be higher than 0 seconds! Aborting..");
           }
        });
        rootCommand.Add(timeOut);
        rootCommand.Add(saveName);
        ParseResult result = rootCommand.Parse(args);
        try
        {
            if (result.Errors.Count == 0)
            {
                // start steam first to mount proton windows prefix
                EnsureSteamStarted();
                Process process = new();
                // const string bmmOptions = "nyyyny\nynynnn";
                var guid = result.GetRequiredValue(saveName);
                var exportSavesPath = result.GetRequiredValue(buildDir);
                var parsedBMMPath = result.GetRequiredValue(bmmPath);
                var parsedBalatroSavesPath = result.GetRequiredValue(balatroSavePath);
                var userTimeout = result.GetRequiredValue(timeOut);
                BackupBalatroSaves(parsedBalatroSavesPath, guid);
                // start balatro first with mods to refresh dump files
                StartBalatroWithTimeout(userTimeout);
                PrepareForSaveTransfer(parsedBalatroSavesPath, guid);
                // TODO : create equivalent PS1 script for windows users
                process.StartInfo.FileName = Path.Combine(parsedBMMPath,"compile_latest_apk.sh");
                process.StartInfo.UseShellExecute = true;
                process.Start();
                // StreamWriter writer = process.StandardInput;
                // foreach (var item in bmmOptions.ToCharArray())
                // {
                //     writer.Write($"{item}\n");
                // }
                process.WaitForExit();
                Console.WriteLine($"Built balatro at path {Path.Join(parsedBMMPath, "balatro.apk")}");
                // zip directory for use with the BM3 mobile app first
                ZipBuiltFolder(parsedBalatroSavesPath, Path.Combine(exportSavesPath, $"Balatro {currentDate}-{guid}.zip"));
                // then have script delete folder with GUID for current user.
                CleanupAfterBuild(parsedBalatroSavesPath, guid);
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

    private static void EnsureSteamStarted()
    {
        // check first if steam is already running
        Process[] processList = Process.GetProcessesByName("steam");
        if (processList.Length > 0 )
        {
           // do nothing, steam is already running
           return;
        }
        var process = new Process();
        process.StartInfo.FileName = "steam";
        process.StartInfo.UseShellExecute = true;
        process.Start();
    }
    private static void ZipBuiltFolder(string userSavesPath, string exportSavesPath)
    {
        
        ZipFile.CreateFromDirectory(userSavesPath,exportSavesPath,CompressionLevel.Optimal,false);
    }

    private static string CreateDirIfNotExists(string path)
    {
        if (!Directory.Exists(path))
        {
            Directory.CreateDirectory(path);
        }
        return path;
    }

    private static string GetBalatroDefaultSavePath()
    {
        var defaultSavePath = "";
        var homeDir = Environment.GetFolderPath(Environment.SpecialFolder.UserProfile);
        if (RuntimeInformation.IsOSPlatform(OSPlatform.Linux))
        {
            // on linux, prepend default windows roaming path with prefix location 
            defaultSavePath = $"{homeDir}/.steam/steam/steamapps/compatdata/2379780/pfx/drive_c/users/steamuser/AppData/Roaming/Balatro";
        }
        else if (RuntimeInformation.IsOSPlatform(OSPlatform.Windows))
        {
            var roamingDir = Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData);
            defaultSavePath = Path.Combine(roamingDir, "Balatro");
        }

        else if (RuntimeInformation.IsOSPlatform(OSPlatform.OSX))
        {
            defaultSavePath = $"{homeDir}/Library/Application Support/Balatro";
        }

        if (string.IsNullOrEmpty(defaultSavePath))
        {
            throw new UnsupportedOSExecption("Cannot run script on this OS!");
        }

        return defaultSavePath;


    }

    private static void PrepareForSaveTransfer(string ParsedBalatroSavePath, string guid)
    {

        // before build with BMM, prepare copy for main machine
        var balatroSavePathInfo = new DirectoryInfo(ParsedBalatroSavePath) ?? throw new Exception($"{ParsedBalatroSavePath} is null or invalid! ");
        var pathParent = balatroSavePathInfo.Parent ?? throw new Exception($"{ParsedBalatroSavePath} is null or invalid! ");
        var newName = Path.Join(pathParent.FullName, $"{balatroSavePathInfo.Name} Copy {guid}");
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
        repo = "https://github.com/ethangreen-dev/lovely-injector",
        version = "{{currentLovelyVersion}}",
        mod_dir = "/data/data/com.unofficial.balatro/files/save/zip/Mods",
        }
        """);
        // create SMODS folder
        var smodsPath = Path.Combine([currentSavePath, "SMODS"]);
        Directory.CreateDirectory(smodsPath);
        // Directory.Delete(smodsPath,true);
        // move release.lua and version.lua to smods folder in the root saves directory
        // CopyDirectory(Path.Combine([modsDir, "Steamodded"]), smodsPath, true);
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

    private static void StartBalatroWithTimeout(int timeoutSeconds = 15)
    {
        var balatroProcess = new Process();

        balatroProcess.StartInfo.FileName = "steam";
        balatroProcess.StartInfo.UseShellExecute = true;
        balatroProcess.StartInfo.Arguments = $"-applaunch {balatroAppID}";
        balatroProcess.Start();
        Thread.Sleep(TimeSpan.FromSeconds(timeoutSeconds));
        // after we wait for timeout, kill balatro process only
        var extraprocesses = Process.GetProcessesByName("Balatro.exe");
        foreach (var p in extraprocesses)
        {
            p.Kill();
        }
    }

    private static void BackupBalatroSaves(string ParsedBalatroSavePath, string guid)
    {
        // before build with BMM, prepare copy for main machine
        var balatroSavePathInfo = new DirectoryInfo(ParsedBalatroSavePath) ?? throw new Exception($"{ParsedBalatroSavePath} is null or invalid! ");
        var pathParent = balatroSavePathInfo.Parent ?? throw new Exception($"{ParsedBalatroSavePath} is null or invalid! ");
        var newName = Path.Join(pathParent.FullName, $"{balatroSavePathInfo.Name} Copy {guid}");
        var currentSavePath = balatroSavePathInfo.FullName;
        CopyDirectory(currentSavePath, newName, true);
    }

    private static string PathCombine(string firstPath, string[] paths)
    {
        return Path.Combine([firstPath, .. paths]);
    }
    private static void CleanupAfterBuild(string parsedBalatroSavePath, string guid)
    {
        var balatroSavePathInfo = new DirectoryInfo(parsedBalatroSavePath) ?? throw new Exception($"{parsedBalatroSavePath} is null or invalid! ");
        var pathParent = balatroSavePathInfo.Parent ?? throw new Exception($"{parsedBalatroSavePath} is null or invalid! ");
        var backupName = Path.Join(pathParent.FullName, $"{balatroSavePathInfo.Name} Copy {guid}");
        var balatroTemp = Path.Combine( pathParent.FullName, "Balatro temp" );
        // swap balatro folder and backed up balatro folder quickly

        // we zipped main game folder in previous step, now we have to restore it
        // first move main balatro folder to temp copy
        Directory.Move(parsedBalatroSavePath, balatroTemp);
        // then rename the backup to the main balatro folder
        Directory.Move(backupName, parsedBalatroSavePath);
        // finally, delete balatro temp to reduce bloat
        Directory.Delete(balatroTemp,true);
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