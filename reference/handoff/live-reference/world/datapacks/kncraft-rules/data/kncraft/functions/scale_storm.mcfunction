# Persistent modifiers survive phase changes; vanilla health cap still applies in large phases.
attribute @s minecraft:generic.max_health modifier add 65b697a5-2a47-49fb-a6f1-bc06230990f4 kncraft_storm_health 0.5 multiply_base
attribute @s minecraft:generic.attack_damage modifier add 766a37b6-5376-47df-bd46-9f756891a4b1 kncraft_storm_damage 0.5 multiply_base
attribute @s minecraft:generic.armor modifier add 7b9a2e4e-d880-4584-bb07-85ff369f12fd kncraft_storm_armor 4 add
execute store result entity @s Health float 1 run attribute @s minecraft:generic.max_health get 1
tag @s add kncraft_storm_balanced
