# Limbus-E.G.O-Fabric — Limbus E.G.O Unified Mod (Fabric)

[繁體中文](README.md) | English

A Fabric mod that brings Limbus Company's E.G.O weapons and E.G.O gifts into Minecraft,
fully ported from the Paper plugin [Limbus-E.G.O](https://github.com/Crossing-Dead-Development/Limbus-E.G.O) v1.3.0.

> ⚠️ Work in progress: currently Phase 0 scaffolding with no game content yet. Features will be ported in three phases — see the roadmap below.

- **Minecraft version**: 1.21.4
- **Loader**: Fabric Loader 0.16.9+
- **Required dependencies**: [Fabric API](https://modrinth.com/mod/fabric-api), [Accessories](https://modrinth.com/mod/accessories)
- **Installation**: the mod and its dependencies must be installed on **both client and server**
- **Java**: 21

## Roadmap

| Phase | Content | Status |
|-------|---------|--------|
| Phase 1 (v0.1.x) | 12-status system, Sanity (SAN), 8 E.G.O weapons, `/limbusego weapon` commands & catalog | 🚧 In progress |
| Phase 2 (v0.2.x) | Accessories gift slots, Vestige upgrades, 80 E.G.O gifts, `/limbusego gift` commands | ⬜ Not started |
| Phase 3 (v1.0.0) | Gacha chest / Thread lottery chest / Shop chest, gift catalog, language switching | ⬜ Not started |

## Differences from the Paper plugin

- Items are native mod items — **no server resource pack needed** (textures are built in)
- Gifts are worn via [Accessories](https://modrinth.com/mod/accessories) slots (5 universal slots) instead of the custom `/accessories` GUI; **the gift menu opener item no longer exists**
- Items live in two custom creative tabs ("E.G.O Weapons" / "E.G.O Gifts") instead of vanilla tabs
- **The Chatuhu and Apocalypse Bird bundle items are not ported**
- Save data is not interchangeable between the two platforms

## Development

```
.\gradlew.bat build      # Build
.\gradlew.bat runClient  # Launch dev client
.\gradlew.bat runServer  # Launch dev server
```

## License

MIT
