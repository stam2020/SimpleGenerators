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

Make sure to read this carfuly before starting your project!
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

To define a new generator, under the generator field in the config add the rank of the generator, and under it the information. For example:
```yaml
1:
    name: "My First Generator!"
    lore: "The lore for your generator"
    block: "minecraft:cake"
    drop: "minecraft:candle"
    upgrade_price: 500
```
This will create a generator made of cake called "My First Generator!" with the lore "The lore for your generator" that generates candles, and costs 500 to upgrade.

To define a new sellable item, under the items field in the config add the id of the sellable item, and under it the information. For example:
```yaml
candle:
    sell_price: 20
```
This will make the candle item sell for 20.

To define a sellwand, under the sellwands field in the config, add any name you want for the sellwand, and under it the information. For example:
 
```yaml
x3SellWand:
    name: "My First Sellwand!"
    item: "minecraft:blaze_rod"
    multiplier: 3
```
This will create a blaze rod sellwand, called "My First Sellwand!", with a sell mutiplier of 3.

To change the frequency of the drops of the generators, change the drop_frequency field in the config. For example:
```yaml
drop_frequency: 100
```
Will make generators drop every 100 ticks, meaning 5 seconds.

If you want to require players to be on the server for their generators to work, set the field ```generate_offline``` to false. Be careful, turning it to true might cause alot of lag!
 
You can also limit the maximum amount of generators a player can place down, by giving them or their rank the permission node: ```SimpleGenerators.max_gen.NUMBER```. For example the permission node ```SimpleGenerators.max_gen.5``` will allow them to place down 5 generators.
 
To reload the config in-game, run:
```sh
/SimpleGenerators reload
```



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
