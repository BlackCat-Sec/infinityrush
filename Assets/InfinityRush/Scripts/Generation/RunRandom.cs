using System;

namespace InfinityRush.Generation
{
    public class RunRandom
    {
        private Random _random;
        public int Seed { get; private set; }

        public RunRandom(int seed)
        {
            Seed = seed;
            _random = new Random(seed);
        }

        public void SetSeed(int seed)
        {
            Seed = seed;
            _random = new Random(seed);
        }

        public int NextInt(int minValue, int maxValue)
        {
            return _random.Next(minValue, maxValue);
        }

        public float NextFloat()
        {
            return (float)_random.NextDouble();
        }

        public bool NextBool()
        {
            return _random.Next(2) == 1;
        }
    }
}
