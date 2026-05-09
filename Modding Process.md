# Here is my entire process

- Run the game with mods on PC. Get to the splash screen and ALT+F4 the game, as long as it loads up with the mods to the title you're good.
- Run balatro-mobile-maker with your phone plugged in. create the apk using your preferences from the terminal. (dont worry about external storage patch)

- move the apk using the mobile maker to your phone

- when it asks about steam save transfer PAUSE. DONT UNPLUG PHONE, DONT CLOSE TERMINAL.
- Minimize terminal
- Open file explorer, navigate to C:\Users\[YOURNAME]\AppData\Roaming\Balatro\Mods\lovely\dump
- CTRL+A and then CTRL+X on the entire contents of the \dump\ folder mentioned.
- go back to C:\Users\[YOURNAME]\AppData\Roaming\Balatro\  CTRL+V all the files into that folder alongside the Mods folder
- The files nativefs.lua from C:\Users\YOURNAME\AppData\Roaming\Balatro\Mods\smods\libs\nativefs and json.lua from C:\Users\YOURNAME\AppData\Roaming\Balatro\Mods\smods\libs\json move them to (i didnt copy i used ctrl+x) C:\Users\[YOURNAME]\AppData\Roaming\Balatro

- Create a file in the same Balatro folder called "lovely.lua" and paste this :

<pre>
<code>
 return {
 repo = "<https://github.com/ethangreen-dev/lovely-injector>",
 version = "0.7.1",
 mod_dir = "/data/data/com.unofficial.balatro/files/save/game/Mods",
 }
</code>
</pre>

- I just changed the version to the latest and directed it to this folder which worked for me^^ (current latest version is 0.8.0)

- ctrl+x the file version.lua from your C:\Users\YOURNAME\AppData\Roaming\Balatro\Mods\smods then the go to C:\Users\YOURNAME\AppData\Roaming\Balatro creating a few folder called SMODS all caps and paste the file into that SMODS folder.

- Make sure you remember the balatro mobile compat mod

- if you dont use Talisman skip the next step /

- if you have any mods that use talisman then you need to create a folder under C:\Users\YOURNAME\AppData\Roaming\Balatro\ called nativefs and CTRL+X the nativefs.lua from C:\Users\Ethan\AppData\Roaming\Balatro\Mods\Talisman into the new C:\Users\YOURNAME\AppData\Roaming\Balatro\nativefs folder.
- At this point, all of the files are in the correct location on PC. YOU SHOULD NOW BE READY TO TRANSFER YOUR SAVES FROM THE TERMINAL OF MOBILE MAKER.

- When it completes, your game should be attempting to load any mods when you run it. if you have any mod crash screens YOU HAVE MOVED EVERYTHING PROPERLY. ONE OF YOUR MODS IS THE PROBLEM NOW.

- (if you need to check, in a new terminal run adb shell and then run-as com.unofficial.balatro find and you should see everything. Don't believe that this process is how its done? just install the apk but dont transfer saves and run the same command. you wont see much at all. )

- If you need to DELETE a mod then i've been uninstalling the apk, moving the nativefs, json and version.lua files to their original locations and then deleting everything under C:\Users\YOURNAME\AppData\Roaming\Balatro\ except the mods folder (essentially resetting it back to step 1. I then rerun the game and start all over. :)

- if you only need to fix something in one of the files like changing a line of code in a .lua file then just rerun the mobile maker without deleting the apk and say no to everything except file transfer. it'll push the new copy over.

- I am only spelling it out like this in case anyone is as dumb as I am, because I over complicate things ^as you maybe see. But I'm on the right track now. Still navigating my own mod issues at the moment but this process works.
