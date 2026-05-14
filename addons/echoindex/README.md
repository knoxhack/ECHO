<!-- CURSEFORGE_README_START -->
# ECHO: Index

![ECHO: Index banner](docs/curseforge/echoindex-banner.png)

**A shared item, recipe, usage, and archive reference layer for ECHO survival planning.**

![ECHO: Index feature overview](docs/curseforge/echoindex-features.png)

## CurseForge Summary

Searchable ECHO index for items, recipes, uses, sources, archive context, and Terminal reference surfaces.

## Overview

ECHO: Index is the reference layer for players who need to understand a large modular ECHO pack. It collects item entries, recipe and usage views, source notes, tracking actions, and addon-provided context into a searchable interface.

The addon complements ECHO Terminal's Recipe Index and Lens inspection actions. Instead of forcing players to memorize which chapter owns every material, machine, cache, or process, Index gives the ecosystem a common lookup surface.

For pack authors, ECHO: Index is a clean place to publish explainers for special recipes, locked outputs, machine categories, and non-vanilla acquisition routes without burying everything in tooltips.

## Main Features

- Standalone visual catalog screen with searchable item grid, category filters, bookmarks, and live recipe/usage/source cards.
- Inventory overlay with the same search and detail behaviour for quick lookup while managing items.
- Recipes, uses, tracking, source records, and archive-style notes.
- Provider-backed data so each addon can publish its own process context.
- Optional Terminal and RenderCore integrations.
- Useful companion to Lens actions for recipes, uses, and tracking.

## How It Plays

- Press the **Open Index Catalog** key (default `G`) to open the full-screen visual catalog anywhere.
- In the catalog, search an item or machine, inspect recipes and uses, then follow source notes to learn which route, process, or chapter unlocks the next step.
- While in an inventory screen, the overlay appears automatically with the same search and detail controls.
- When paired with Lens, looking at a target can jump directly into recipe, usage, or tracking context.

## Requirements

- Minecraft 26.1.2
- NeoForge 26.1.2.29-beta or newer
- Java 25+
- ECHO: Core 1.0.0 or newer
- ECHO: NetCore 1.0.0 or newer

## Recommended Pairings

- ECHO: Terminal for integrated recipe browsing
- ECHO: Lens for inspection actions
- JEI alongside large modpacks

## Compatibility Notes

- Index providers are additive and should tolerate missing sibling addons.
- The addon is informational and does not replace recipe authority from owning mods.

## CurseForge Asset Files

- Banner: `docs/curseforge/echoindex-banner.png`
- Feature image: `docs/curseforge/echoindex-features.png`

<!-- CURSEFORGE_README_END -->
