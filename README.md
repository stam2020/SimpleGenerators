[![Contributors][contributors-shield]][contributors-url]
[![Forks][forks-shield]][forks-url]
[![Stargazers][stars-shield]][stars-url]
[![Issues][issues-shield]][issues-url]
[![MIT License][license-shield]][license-url]



<br />
<p align="center">
  <h3 align="center">Simple Generators</h3>

  <p align="center">
    Make generator servers easily!
    <br />
    <br />
    <a href="https://github.com/stam2020/SimpleGenerators/issues">Report Bug</a>
    ·
    <a href="https://github.com/stam2020/SimpleGenerators/issues">Request Feature</a>
  </p>
</p>

Make sure to read this carefully before starting your project!
<details open="open">
  <summary>Table of Contents</summary>
  <ol>
    <li>
      <a href="#about-the-project">About The Project</a>
      <ul>
        <li><a href="#built-with">Built With</a></li>
      </ul>
    </li>
    <li>
      <a href="#getting-started">Getting Started</a>
      <ul>
        <li><a href="#prerequisites">Prerequisites</a></li>
      </ul>
    </li>
    <li><a href="#usage">Usage</a></li>
    <li><a href="#permissions">Permissions</a></li>
    <li><a href="#roadmap">Roadmap</a></li>
    <li><a href="#contributing">Contributing</a></li>
    <li><a href="#license">License</a></li>
    <li><a href="#contact">Contact</a></li>
  </ol>
</details>




## About The Project

I always believe that everything should be simple.
<br>
So when I discovered that so little generator servers exist, I thought to develop this plugin, and make generator servers accessible to everyone. 

Why you should use this plugin:
* Fast, efficient and powerful
* Very configurable and flexible


I hope that you will find this plugin useful!

### Built With

This plugin is built using
* [Java](https://www.java.com/en/)
* [Spigot](https://www.spigotmc.org/)
* [SQLite](https://www.sqlite.org/index.html)




## Getting Started

To download the plugin, visit [SimpleGenerators](https://www.spigotmc.org/resources/simplegenerators.94621/)

### Prerequisites

Requires the following plugins:
* [Vault](https://dev.bukkit.org/projects/vault)
* Any economy plugin, such as [Essentials](https://www.spigotmc.org/resources/essentialsx.9089/)


## Usage

To define a new generator, run in game the command 
```
/sg generator add <id> <name> <lore> <block> <drop> <upgrade_price> <tier>
```
Where \<id> is a unique name for the generator, \<name> is the name of the generator, \<lore> is its lore, \<block> is what block it is, \<drop> is what is drops, \<upgrade_price> is how much it costs to upgrade it, and \<tier> is how good it is (The higher the better)

If the name, or the lore have a space in them, it's important to put them in quotation marks.

It's also possible to add a generator directly from the config.yml file. The default one has an example of how to define one.

You can remove a generator by either deleting it in the config.yml file, or by using the command
```
/sg generator remove <id>
```
Where \<id> is the id of one of the existing generators.

You can edit a generator either from the config.yml file, or by using the command
```
/sg generator edit <id> <proprety> <value>
```
Which allows you to change a property of a generator. 

You can get one of the generators by using
```
/sg generator get <id>
``` 
Which gives you the generator with the id of <id>

The generators drop items, which must also be defined. You can add an item using the command
```
/sg item add <item> <name> <lore> <sell_price>
```
Where \<item> is the type of item, \<name> is its name, \<lore> is its lore, and \<sell_price> is for how much it sells for when using the /sell command

Just like generators, you can edit and remove items by using
```
/sg item remove <item>
/sg item edit <item> <proprety> <value>
```

It's also possible to define "wands" that can sell every sellable item in a chest. To define a sellwand, you can create one through the config.yml file, or run the command
```
/sg sellwand add <id> <name> <lore> <item> <multiplier>
``` 
Where the id is a unique name you give the sellwand, \<name> is its name, \<lore> is its lore, \<item> is what type of item the sellwand is, and \<mutliplier> is by how much the received from selling will by multiplied (mush be a whole number) 
Similarly to items and generators, you can also edit and remove sellwand using
```
/sg sellwand remove <id>
/sg sellwand edit <id> <proprety> <value>
```

To change the frequency of the drops of the generators, change the drop_frequency field in the config, for example:
```yaml
drop_frequency: 100
```
Will make generators drop every 100 ticks, meaning 5 seconds.

If you want to require players to be on the server for their generators to work, set the field ```generate_offline``` to false. Be careful, setting it to true might cause a-lot of lag!
 
You can also limit the maximum amount of generators a player can place down, by giving them or their rank the permission node: ```simplegenerators.max_gen.NUMBER```. For example the permission node ```simplegenerators.max_gen.5``` will allow them to place down 5 generators.
 
To reload the config in-game, run:
```sh
/sg reload
```

## Permissions

Command | Permission
--- | ---
Add generator | simplegenerators.admin.add.gen
Remove generator | simplegenerators.admin.remove.gen
Edit generator | simplegenerators.admin.edit.gen
Add item | simplegenerators.admin.add.item
Remove item | simplegenerators.admin.remove.item
Edit item | simplegenerators.admin.edit.item
Add sellwand | simplegenerators.admin.add.sellwand
Remove sellwand | simplegenerators.admin.remove.sellwand
Edit sellwand | simplegenerators.admin.edit.sellwand
Reload config | simplegenerators.admin.reload



## Roadmap

See the [open issues](https://github.com/stam2020/SimpleGenerators/issues) for a list of proposed features (and known issues).



## Contributing

All contributions are appreciated and help a-lot!

How to contribute:

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request




## License

Distributed under the GNU License. See `LICENSE` for more information.




## Contact

Discord - **PlainPlaying#2283**

Project Link: [https://github.com/stam2020/SimpleGenerators](https://github.com/stam2020/SimpleGenerators)



[contributors-shield]: https://img.shields.io/github/contributors/stam2020/SimpleGenerators.svg?style=for-the-badge
[contributors-url]: https://github.com/stam2020/SimpleGenerators/graphs/contributors
[forks-shield]: https://img.shields.io/github/forks/stam2020/SimpleGenerators.svg?style=for-the-badge
[forks-url]: https://github.com/stam2020/SimpleGenerators/network/members
[stars-shield]: https://img.shields.io/github/stars/stam2020/SimpleGenerators.svg?style=for-the-badge
[stars-url]: https://github.com/stam2020/SimpleGenerators/stargazers
[issues-shield]: https://img.shields.io/github/issues/stam2020/SimpleGenerators.svg?style=for-the-badge
[issues-url]: https://github.com/stam2020/SimpleGenerators/issues
[license-shield]: https://img.shields.io/github/license/stam2020/SimpleGenerators.svg?style=for-the-badge
[license-url]: https://github.com/stam2020/SimpleGenerators/blob/master/LICENSE
[product-screenshot]: images/screenshot.png
