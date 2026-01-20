# SnowballGame
SnowballGame is a Bukkit plugin that lets you play baseball in Minecraft.
It's compatible with Spigot/Paper 1.21.1.

## SnowballGameV2
This repository contains a Kotlin rewrite of SnowballGame.
The original version can be found at: https://github.com/kanesada2/SnowballGame/

## V2 Update
### For Players
- All items used in SnowballGame have been redesigned. If you have items from previous versions in your inventory, you can convert them to the new versions by running the `/sbg update` command.
- Placed objects like Coach, Base, and Umpire have also been renewed. When you break old Bases or Umpires, they will drop the new items. Old Coaches, however, have become ordinary glowing armor stands—feel free to keep them as decoration or clean them up.
- Bats still look like bows, but the bow-drawing mechanic for batting has been removed. Now, right-clicking once with a bat starts charging a dedicated swing power gauge. Right-click again to stop the gauge, then right-click a third time to swing at the ball, just like before.
- Additionally, swinging the bat while sneaking now allows you to take lighter swings than normal. With maximum power at 100%, sneaking enables swings at 0-50% power, while standing allows 50-100% power swings. Swinging at 40% power or less (that is, sneaking with the gauge at 80% or below) will register as a bunt. Note that it doesn't matter whether you were sneaking while the gauge was moving or after stopping it—only your sneak state at the moment of the swing counts. This means you can pull off a beautiful surprise safety bunt just like Ichiro.- The spin calculation for batted balls has been significantly improved. Foul flies down the line no longer curve back the wrong way mid-flight, making ball trajectories much more realistic.
- Dispensers have become pitchers with better control. Not quite Yoshinobu Yamamoto level, but still an improvement.
- Left-clicking a snowman while holding a ball will turn it into a 'Ballmaker,' allowing you to obtain balls from the snow beneath its feet. **Note** that these come from the snow that was under its feet when dug, not from snow it generates itself. By the way, its default name is 'Ball_Smith'—sounds like someone who might craft premium items for you.
- Fixed the embarrassing typo "Catched!" that appeared when catching a ball. As you might have guessed, I'm Japanese and English isn't my strong suit. I'm truly grateful that translation AI exists now.

### For Server Admins
- A new configuration file called `bounce.yml` has been added, allowing you to configure which blocks give balls special bounce behavior. If you want to assign roles to blocks added in newer Minecraft versions, edit this file.
- The five levels of ball repulsion when hit by a bat have existed for a while, but you can now configure the rebound coefficients for each level in the config.
- A new `Reusable: True/False` setting has been added for pitch types. Balls with this set to true will retain their name even after bouncing, being caught by a glove, or being hit by a bat. I added this feature thinking it might be useful for sports other than baseball.
- Glove catch range can now be configured.
- Bat power and sweet spot distance can now be configured.
- For particle settings, you can now set a `Block` property to spawn particles associated with specific blocks. However, other data types like Color are not supported.

### For Developers
- The entire plugin codebase has been completely rewritten, now in Kotlin. I think it's considerably better than the terrible code it used to be.
- I've tried to keep the interface unchanged for `SnowballGameAPI` class methods and SnowballGame events. However, everything else has changed.
- I've noticed that some of you forked my repository and continued using the plugin while fixing parts that broke with Minecraft version updates. Thank you for taking care of this plugin. It probably goes without saying, but I fully welcome anyone using or modifying any or all of this plugin's source code.
- This will be my last update, but I would be truly happy if someone develops a successor plugin.

### For Everyone
- I've neglected this plugin for a long time, and after this update, SnowballGame will no longer be updated. If it breaks again for any reason in the future, I won't be fixing it. To all the players who have enjoyed SnowballGame over the years—thank you so much!