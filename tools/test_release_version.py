import unittest
from release_version import select_version

class VersionTests(unittest.TestCase):
    def test_first_release_and_monotonic_patch(self):
        self.assertEqual(select_version('1.0.0',[]),'1.0.0')
        self.assertEqual(select_version('1.0.0',['v1.0.9','v1.0.2','v0.2.0','v1.0.11-dev.1']),'1.0.10')
    def test_rerun_reuses_tag_for_exact_commit(self):
        self.assertEqual(select_version('1.0.0',['v1.0.9'],['v1.0.4']),'1.0.4')
    def test_intentional_new_series_and_minimum(self):
        self.assertEqual(select_version('1.1.0',['v1.0.99']),'1.1.0')
        self.assertEqual(select_version('1.0.5',['v1.0.2']),'1.0.5')

if __name__=='__main__':unittest.main()
