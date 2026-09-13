namespace InfinityRush.Scoring
{
    public struct RunStats
    {
        public int currentScore;
        public int highScore;
        public int coinsCollected;
        public float distanceTravelled;
        public int scoreMultiplier;

        public void Reset(int savedHighScore = 0)
        {
            currentScore = 0;
            highScore = savedHighScore;
            coinsCollected = 0;
            distanceTravelled = 0f;
            scoreMultiplier = 1;
        }
    }
}
