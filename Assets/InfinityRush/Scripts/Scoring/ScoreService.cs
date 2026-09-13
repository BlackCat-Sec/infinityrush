using System;
using UnityEngine;
using InfinityRush.Core;

namespace InfinityRush.Scoring
{
    public interface IScoreService
    {
        event Action<int> OnScoreChanged;
        event Action<int> OnCoinsChanged;
        event Action<int> OnMultiplierChanged;

        RunStats Stats { get; }
        int CurrentScore { get; }
        int HighScore { get; }
        int CoinsCollected { get; }
        int Multiplier { get; }

        void StartRun(int initialHighScore = 0);
        void AddDistance(float distanceDelta);
        void AddCoins(int amount);
        void AddScoreBonus(int bonusPoints);
        void SetMultiplier(int multiplier);
        void ResetMultiplier();
    }

    public class ScoreService : MonoBehaviour, IScoreService
    {
        public event Action<int> OnScoreChanged;
        public event Action<int> OnCoinsChanged;
        public event Action<int> OnMultiplierChanged;

        private RunStats _stats;
        public RunStats Stats => _stats;

        public int CurrentScore => _stats.currentScore;
        public int HighScore => _stats.highScore;
        public int CoinsCollected => _stats.coinsCollected;
        public int Multiplier => _stats.scoreMultiplier;

        private float _distanceAccumulator = 0f;

        private void Awake()
        {
            ServiceRegistry.Instance.Register<IScoreService>(this);
        }

        public void StartRun(int initialHighScore = 0)
        {
            _stats.Reset(initialHighScore);
            _distanceAccumulator = 0f;
            OnScoreChanged?.Invoke(_stats.currentScore);
            OnCoinsChanged?.Invoke(_stats.coinsCollected);
            OnMultiplierChanged?.Invoke(_stats.scoreMultiplier);
        }

        public void AddDistance(float distanceDelta)
        {
            if (distanceDelta <= 0f) return;

            _stats.distanceTravelled += distanceDelta;
            _distanceAccumulator += distanceDelta * _stats.scoreMultiplier;

            int points = Mathf.FloorToInt(_distanceAccumulator / Constants.SCORE_DISTANCE_DIVISOR);
            if (points > 0)
            {
                _distanceAccumulator -= points * Constants.SCORE_DISTANCE_DIVISOR;
                _stats.currentScore += points;

                if (_stats.currentScore > _stats.highScore)
                {
                    _stats.highScore = _stats.currentScore;
                }

                OnScoreChanged?.Invoke(_stats.currentScore);
            }
        }

        public void AddCoins(int amount)
        {
            if (amount <= 0) return;

            _stats.coinsCollected += amount;
            AddScoreBonus(amount * Constants.COIN_SCORE_BONUS * _stats.scoreMultiplier);
            OnCoinsChanged?.Invoke(_stats.coinsCollected);
        }

        public void AddScoreBonus(int bonusPoints)
        {
            if (bonusPoints <= 0) return;

            _stats.currentScore += bonusPoints;
            if (_stats.currentScore > _stats.highScore)
            {
                _stats.highScore = _stats.currentScore;
            }
            OnScoreChanged?.Invoke(_stats.currentScore);
        }

        public void SetMultiplier(int multiplier)
        {
            _stats.scoreMultiplier = Mathf.Max(1, multiplier);
            OnMultiplierChanged?.Invoke(_stats.scoreMultiplier);
        }

        public void ResetMultiplier()
        {
            SetMultiplier(1);
        }

        private void OnDestroy()
        {
            ServiceRegistry.Instance.Unregister<IScoreService>();
        }
    }
}
