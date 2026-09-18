import unittest
from thermal_complexity import complexity_catalog, scale_effect


def recipe(item, ingredients, suffix=''):
    return {'id': item + suffix, 'result': item, 'crafting': True,
            'type': 'minecraft:crafting_shapeless',
            'ingredients': [[i] if isinstance(i, str) else i for i in ingredients]}


def tiers(*recipes):
    return complexity_catalog({'items': {r['result']: {'food': True} for r in recipes}, 'recipes': recipes})


class RecipeTierTests(unittest.TestCase):
    def test_repeats_tools_containers_and_tag_options_do_not_inflate_score(self):
        value = tiers(recipe('test:soup', ['test:beet'] * 6 + ['minecraft:bowl',
            'pamhc2foodcore:potitem', ['test:red', 'test:brown']]))['test:soup']
        self.assertEqual(value['ingredients'], 2)
        self.assertEqual(value['tier'], 'simple')

    def test_cheapest_alternate_recipe_and_ingredient_win(self):
        value = tiers(recipe('test:meal', ['test:a', 'test:b', 'test:c', 'test:d']),
                      recipe('test:meal', ['test:a'], '_easy'))['test:meal']
        self.assertEqual(value['tier'], 'simple')
        self.assertEqual(value['recipe'], 'test:meal_easy')
        value = tiers(recipe('test:sauce', ['test:a', 'test:b']),
                      recipe('test:meal', [['test:sauce', 'test:raw'], 'test:c', 'test:d']))['test:meal']
        self.assertEqual(value['prepared_components'], 0)

    def test_prepared_components_count_once_and_cycles_terminate(self):
        values = tiers(recipe('test:sauce', ['test:a', 'test:b']),
                       recipe('test:meal', ['test:sauce', 'test:c', 'test:d']),
                       recipe('test:cycle_a', ['test:cycle_b', 'test:a']),
                       recipe('test:cycle_b', ['test:cycle_a', 'test:b']))
        self.assertEqual(values['test:meal']['tier'], 'prepared')
        self.assertEqual(values['test:meal']['score'], 4)
        self.assertEqual(values['test:cycle_a']['tier'], 'simple')

    def test_bonuses_preserve_sign_and_cap_duration(self):
        self.assertEqual(scale_effect(.2, 1800, 'simple'), (.2, 1800))
        self.assertEqual(scale_effect(-.2, 1200, 'prepared'), (-.25, 1500))
        self.assertEqual(scale_effect(.2, 2100, 'elaborate'), (.3, 2400))


if __name__ == '__main__':
    unittest.main()
