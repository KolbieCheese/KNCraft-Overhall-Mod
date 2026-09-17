attribute @s minecraft:generic.max_health modifier add 7b12d760-cbf3-4489-a04f-7a01d1076982 kncraft_community 1 multiply_base
execute store result entity @s Health float 1 run attribute @s minecraft:generic.max_health get 1
tag @s add kncraft_balanced
