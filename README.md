A change of plans
=================

The code here is so difficult to work with that I eventually decided to rewrite this plugin. For the sake of
keeping a history, I'm going to leave this repo up with the original contents of my README below, but you
can find my new plugin [right over here](https://github.com/TheArchives/ArchBlock).

---

WatchBlock Refired - That other fork
====================================

WatchBlock provides a rather specific mode of protecting blocks, designed
for all kinds of Bukkit-based Minecraft servers. It provides per-block protection,
so that every block someone places is protected, and provides a "friends list" for
every player, so they can whitelist other players that they'd like to build with.

WatchBlock is one of those special, essential plugins. Unfortunately, it's been
abandoned twice. The initial project is no longer available on BukkitDev, and
the "Refire" fork
[has been officially abandoned by its developer](http://dev.bukkit.org/bukkit-plugins/watchblock-refired/?comment=98).

So, who owns this?
==================

The credit for the "refire" fork goes to **enigma617** and **slade87**. Unfortunately, my efforts
to contact either of them have been in vain, and I have received no responses. Therefore,
if one of them should stumble upon this repository, it would be appreciated if they could
raise a ticket or contact me in some other way, and I'll be happy to take down this repo
or rewrite some of their code if they feel that it shouldn't be here.

Unfortunately, this plugin is massively essential to providing security to my server
network, and it's got plenty of bugs - so I've decompiled the JAR, which appears to
contain absolutely no levels of obfuscation, and I'm hoping to improve the plugin and
fix some of the many bugs and gripes I've had with it.

Alright, so what now?
=====================

As this code is decompiled, it does not contain any comments. As such, I'm aware that the
original developer of this plugin considered his code to be of poor quality, and with a
cursory look over it, I can't say that I disagree with that sentiment. It may get to the
point where a total rewrite is preferable, and I'm happy to do that, but I would rather
not deal with the migration pains that that would cause, so I'm going to try to fix this
plugin first.

This will probably require quite a lot of updating, as new versions of plugins and the Bukkit
API have been released since this plugin's final release, back in the summer of 2013. My list
of goals goes somewhat like this.

* Fix all of the obvious code errors
* Go over the set of bugs I've discovered and attempt to fix them
* Add various features that I've been looking for for years

I don't have more detail than this right now. I guess it's time to get to work!

What if I want to use this?
===========================

I won't stop you. You can compile your jar in three easy steps.

1. Clone this repository
2. Run `gradlew shadowjar`, or `gradlew.bat shadowjar` on Windows
3. When everything has finished, you'll find your jar in `build/libs`.

As of the initial commit, this plugin won't compile. I'll get to it!
