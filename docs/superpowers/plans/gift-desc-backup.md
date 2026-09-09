# 飾品 .desc 備份（Task 6 移除自 lang，Task 7-14 逐組轉成結構化 describe()）

| id | zh_tw | en_us |
|---|---|---|
| `ardent_flower` | 被動：免疫火焰傷害｜攻擊：施加燒傷 2·2｜攻擊燒傷中且生命低於 30% 的目標：+30% 傷害 | Passive: fire immunity | On attack: apply Burn 2·2 | Vs. burning targets below 30% HP: +30% damage |
| `ashes_to_ashes` | 攻擊燒傷中目標：疊加燒傷 2·1 | Attacking a burning target: stack Burn 2·1 |
| `bloodflame_sword` | 攻擊：施加燒傷 3·2｜獲得 1 點 SAN | On attack: apply Burn 3·2 | gain 1 SAN |
| `dust_to_dust` | 攻擊：施加燒傷 3·2｜擊殺：對 3 格內敵人擴散燒傷 3·2 | On attack: apply Burn 3·2 | On kill: spread Burn 3·2 to foes within 3 blocks |
| `glimpse_of_flames` | 攻擊燒傷中目標：引爆燒傷造成真傷並施加易損 1·2 | Attacking a burning target: detonate Burn for true damage and apply Fragile 1·2 |
| `hot_n_juicy_drumstick` | 被動：飽食度不流失｜攻擊燒傷中目標：延長燒傷 2 層 | Passive: saturation never drops | Attacking a burning target: extend Burn by 2 counts |
| `pain_of_stifled_rage` | 攻擊燒傷中目標：獲得強壯 2·1；否則施加燒傷 2·2 | Attacking a burning target: gain Power 2·1; otherwise apply Burn 2·2 |
| `royal_jelly_perfume` | 被動：附近蜜蜂不攻擊｜受擊：對燒傷中的攻擊者 -15% 傷害；並施加燒傷 2·2 | Passive: nearby bees stay calm | When hit: -15% damage from a burning attacker, and apply Burn 2·2 to the attacker |
| `crystallized_blood` | 被動：每 5 秒消耗全部流血層數回復生命（回復量為層數一半，上限 4，隨升級提升） | Passive: every 5s consume all Bleed counts to heal (half the counts, cap 4, scales with upgrade) |
| `la_manchaland_all_day_pass` | 被動：速度 I、跳躍提升 I｜攻擊流血中目標：獲得呼吸法 2·2 | Passive: Speed I, Jump Boost I | Attacking a bleeding target: gain Poise 2·2 |
| `la_manchaland_standard_pass` | 被動：速度 I｜攻擊流血中目標：獲得呼吸法 1·1 | Passive: Speed I | Attacking a bleeding target: gain Poise 1·1 |
| `mask_of_the_parade` | 被動：潛行時持續隱身｜攻擊：施加流血 3·3｜擊殺：對 5 格內敵人擴散流血 2·2 | Passive: invisibility while sneaking | On attack: apply Bleed 3·3 | On kill: spread Bleed 2·2 to foes within 5 blocks |
| `millarca` | 攻擊：施加流血 2·2｜攻擊流血中目標：偷取 1 點生命（隨升級提升） | On attack: apply Bleed 2·2 | Attacking a bleeding target: steal 1 HP (scales with upgrade) |
| `sanguine_blossom_bolus` | 非戰鬥時持續緩慢回血｜攻擊：施加流血 2·2 | Slowly regenerate while out of combat | On attack: apply Bleed 2·2 |
| `artistic_sense` | 攻擊：施加沉淪 2·2｜攻擊沉淪中或抑鬱目標：+25% 傷害 | On attack: apply Sinking 2·2 | Vs. sinking or depressed targets: +25% damage |
| `black_sheet_music` | 攻擊：施加沉淪 3·3｜攻擊抑鬱或沉淪≥4 目標：+25% 傷害 | On attack: apply Sinking 3·3 | Vs. depressed or Sinking≥4 targets: +25% damage |
| `broken_compass` | 攻擊：25% 機率施加沉淪 2·3 | On attack: 25% chance to apply Sinking 2·3 |
| `cold_illusion` | 攻擊：施加沉淪 2·2 與束縛 1·2 | On attack: apply Sinking 2·2 and Bind 1·2 |
| `distant_star` | 攻擊沉淪中目標：獲得 1 SAN 並延長沉淪 1 層 | Attacking a sinking target: gain 1 SAN and extend Sinking by 1 count |
| `frozen_cries` | 受擊：對攻擊者施加沉淪 3·2 | When hit: apply Sinking 3·2 to the attacker |
| `mental_corruption_boosting_gas` | 攻擊：施加沉淪 2·2；目標為玩家時額外 -1 SAN | On attack: apply Sinking 2·2; if the target is a player, -1 SAN |
| `rags` | 攻擊沉淪中目標：+7.5% 傷害並獲得 1 SAN | Attacking a sinking target: +7.5% damage and gain 1 SAN |
| `rest` | 被動：靜止時生命再生 I｜攻擊沉淪中目標：+15% 傷害 | Passive: Regeneration I while standing still | Attacking a sinking target: +15% damage |
| `tangled_bones` | 攻擊：施加沉淪 2·2｜攻擊抑鬱目標：+15% 傷害 | On attack: apply Sinking 2·2 | Vs. depressed targets: +15% damage |
| `dry_to_the_bone_breast` | 被動：飽食度不流失、飽足時力量 I｜攻擊破裂中目標：延長破裂 2 層 | Passive: saturation never drops, Strength I while full | Attacking a ruptured target: extend Rupture by 2 counts |
| `ebony_brooch` | 被動：夜視｜攻擊：施加破裂 2·2；15% 機率追加束縛 1·2 | Passive: Night Vision | On attack: apply Rupture 2·2; 15% chance to add Bind 1·2 |
| `flower_in_the_mirror` | 攻擊：施加破裂 2·2｜被動：每 5 秒對 5 格內敵人施加破裂 2·1 | On attack: apply Rupture 2·2 | Passive: every 5s apply Rupture 2·1 to foes within 5 blocks |
| `harestride` | 被動：速度 II、跳躍提升 I｜速度效果中攻擊：施加破裂 2·2 | Passive: Speed II, Jump Boost I | Attacking while under Speed: apply Rupture 2·2 |
| `moon_in_the_water` | 被動：夜視｜攻擊破裂≥3 目標：獲得呼吸法 2·1 | Passive: Night Vision | Attacking a Rupture≥3 target: gain Poise 2·1 |
| `ruin` | 攻擊：施加破裂 3·2｜對已破裂目標追加脆弱 1·1，但自身損失 0.5 生命 | On attack: apply Rupture 3·2 | Vs. already-ruptured targets: add Fragile 1·1 but lose 0.5 HP |
| `smoking_gunpowder` | 攻擊：施加破裂 2·2，並獲得迅捷 1·2 | On attack: apply Rupture 2·2 and gain Haste 1·2 |
| `strange_glyph_inscriptions` | 攻擊破裂中目標：+20% 傷害並延長破裂 1 層 | Attacking a ruptured target: +20% damage and extend Rupture by 1 count |
| `strange_glyph_talisman` | 擊殺：對 5 格內敵人擴散破裂 3·2 | On kill: spread Rupture 3·2 to foes within 5 blocks |
| `thunderbranch` | 攻擊：施加破裂 2·2；10% 機率召喚閃電並追加破裂 2·1 | On attack: apply Rupture 2·2; 10% chance to call lightning and add Rupture 2·1 |
| `chief_butlers_secret_arts` | 攻擊：施加束縛 2·2｜擊殺：回復 2 點生命 | On attack: apply Bind 2·2 | On kill: restore 2 HP |
| `green_spirit` | 攻擊：施加震顫 2·2 | On attack: apply Tremor 2·2 |
| `nixie_divergence` | 攻擊：施加震顫 2·2 | On attack: apply Tremor 2·2 |
| `sour_liquor_aroma` | 攻擊：施加震顫 2·1｜攻擊震顫≥3 目標：+20% 傷害 | On attack: apply Tremor 2·1 | Vs. Tremor≥3 targets: +20% damage |
| `sownpour` | 攻擊：施加震顫 3·2｜30% 機率連鎖打擊附近敵人（50% 傷害）並追加震顫 2·1 | On attack: apply Tremor 3·2 | 30% chance to chain-strike nearby foes (50% damage) and add Tremor 2·1 |
| `piece_of_crumbled_egg` | 死亡時：對殺手落雷並施加震顫 5·3 | On death: strike the killer with lightning and apply Tremor 5·3 |
| `handheld_mirror` | 受擊：對攻擊者施加束縛 2·2 與脆弱 1·2 | When hit: apply Bind 2·2 and Fragile 1·2 to the attacker |
| `cask_spirits` | 攻擊：獲得呼吸法 2·2｜自身呼吸法≥4 時攻擊額外獲得 1 SAN | On attack: gain Poise 2·2 | Attacking while your Poise≥4: also gain 1 SAN |
| `clear_mirror_calm_water` | 攻擊：獲得呼吸法 3·2｜擊殺：獲得強壯 3·2 | On attack: gain Poise 3·2 | On kill: gain Power 3·2 |
| `emerald_elytra` | 被動：緩降｜疾跑中攻擊：獲得呼吸法 3·2 | Passive: Slow Falling | Attacking while sprinting: gain Poise 3·2 |
| `finifugality` | 攻擊：獲得呼吸法 2·2｜呼吸法達 5 時額外獲得強壯 2·1 | On attack: gain Poise 2·2 | At Poise 5: also gain Power 2·1 |
| `keenbranch` | 攻擊：20% 機率 +30% 傷害並獲得呼吸法 1·1 | On attack: 20% chance for +30% damage and gain Poise 1·1 |
| `nebulizer` | 被動：每 5 秒使自身與 5 格內玩家獲得呼吸法 2·2 | Passive: every 5s grant Poise 2·2 to self and players within 5 blocks |
| `cqc_manual` | 近戰攻擊：獲得呼吸法 2·2 | On melee attack: gain Poise 2·2 |
| `bloody_gadget` | 被動：每 5 秒獲得強壯 2·2 | Passive: every 5s gain Power 2·2 |
| `dreaming_electric_sheep` | 被動：緩降｜擊殺：獲得強壯 2·3 | Passive: Slow Falling | On kill: gain Power 2·3 |
| `dueling_manual_book_3` | 被動：每 5 秒獲得強壯 2·2｜受擊：25% 機率獲得迅捷 2·3 | Passive: every 5s gain Power 2·2 | When hit: 25% chance to gain Haste 2·3 |
| `illusory_hunt` | 攻擊：20% 機率獲得強壯 2·2 | On attack: 20% chance to gain Power 2·2 |
| `late_bloomers_tattoo` | 生命低於 50% 時攻擊：獲得強壯 2·2 與守護 2·2 | Attacking below 50% HP: gain Power 2·2 and Protection 2·2 |
| `hardship` | SAN≥40 時攻擊：獲得強壯 2·1｜擊殺：獲得 2 SAN | Attacking while SAN≥40: gain Power 2·1 | On kill: gain 2 SAN |
| `phantom_pain` | 攻擊：+15% 傷害 | On attack: +15% damage |
| `tenacity_bolus` | 受擊：獲得守護 2·2 | When hit: gain Protection 2·2 |
| `the_book_of_vengeance` | 受擊：獲得強壯 2·2 與守護 1·2 | When hit: gain Power 2·2 and Protection 1·2 |
| `special_contract` | 攻擊：施加脆弱 2·2 | On attack: apply Fragile 2·2 |
| `plume_of_proof` | 攻擊：施加束縛 1·2 並獲得迅捷 1·2 | On attack: apply Bind 1·2 and gain Haste 1·2 |
| `spicebush_branch` | 被動：中毒時轉化為回血效果｜每 5 秒獲得迅捷 2·3 | Passive: convert Poison into healing | every 5s gain Haste 2·3 |
| `carmilla` | 攻擊滿血目標：+20% 傷害 | Attacking a full-HP target: +20% damage |
| `e_type_dimensional_dagger` | 攻擊：獲得充能 2·2｜25% 機率瞬移背刺（額外傷害）並改獲充能 4·2 | On attack: gain Charge 2·2 | 25% chance to teleport-backstab (bonus damage) and gain Charge 4·2 instead |
| `trauma_shield` | 受傷時：每 60 秒吸收一次傷害並獲得 2 SAN | When hurt: every 60s absorb one hit and gain 2 SAN |
| `blue_zippo_lighter` | 攻擊：20% 機率施加燒傷 2·2｜右鍵：每 8 秒點燃附近目標 | On attack: 20% chance to apply Burn 2·2 | Right-click: every 8s ignite a nearby target |
| `child_within_a_flask` | 受致命傷時：每 2 分鐘免死一次，回復 4 生命並擊退附近敵人 | On lethal damage: cheat death once every 2 min, restore 4 HP and knock back nearby foes |
| `golden_urn` | 擊殺時：15% 機率複製目標掉落物 | On kill: 15% chance to duplicate the target's drops |
| `homeward` | 脫戰 5 秒後：每次脫戰回復最多 50% 最大生命（每次戰鬥限一次） | 5s out of combat: heal up to 50% max HP (once per combat) |
| `lithograph` | 擊殺時：回復生命與飽食度 | On kill: restore health and hunger |
| `oracle` | 潛行時：每 10 秒使周圍生物發光 3 秒 | While sneaking: every 10s make nearby creatures glow for 3s |
| `prejudice` | 攻擊血量比例低於自己的目標時：最多 +30% 傷害 | Vs. targets with a lower HP ratio than you: up to +30% damage |
| `piece_of_relationship` | 被動：吸引經驗球並使隊友再生｜擊殺時：經驗 +50% | Passive: pull in XP orbs and regenerate allies | On kill: +50% XP |
| `rusty_commemorative_coin` | 攻擊低血量目標：每 8 秒處決一次｜擊殺時：獲得強壯 2·2 | Attacking a low-HP target: execute once every 8s | On kill: gain Power 2·2 |
| `someones_device` | 被動：吸引附近掉落物與經驗球 | Passive: pull in nearby drops and XP orbs |
| `sunshower` | 被動：晴天每 5 秒獲得迅捷 2·2，雨天再生｜雨天攻擊：獲得強壯 2·1 | Passive: every 5s gain Haste 2·2 in clear weather, Regeneration in rain | Attacking in rain: gain Power 2·1 |
| `trial_plan_guide` | 擊殺時：經驗 +50%｜被動：村莊英雄 I | On kill: +50% XP | Passive: Hero of the Village I |
| `endless_hunger` | 被動：飢餓不虛弱、免疫飢餓傷害｜飽食度高時攻擊：獲得強壯 2·1 | Passive: no hunger weakness, immune to starvation | Attacking while well-fed: gain Power 2·1 |
| `flower_mound` | 被動：生命再生 I｜擊殺時：對附近敵人施加沉淪 2·2 | Passive: Regeneration I | On kill: apply Sinking 2·2 to nearby foes |
| `jin_gang_bolus` | 被動：吸收 I｜每 5 秒獲得守護 2·3 | Passive: Absorption I | every 5s gain Protection 2·3 |
| `piece_of_a_torn_summer` | 受火焰或熔岩傷害時：獲得強壯 2·2 | When taking fire or lava damage: gain Power 2·2 |
| `tranquil_lotus_bolus` | 被動：每 5 秒獲得守護 2·2，每 10 秒回復 1 SAN | Passive: every 5s gain Protection 2·2, every 10s restore 1 SAN |
