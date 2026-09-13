using System;
using System.Collections.Generic;
using UnityEngine;

namespace InfinityRush.PowerUps
{
    public class PowerUpController : MonoBehaviour
    {
        public event Action<PowerUpType, float> OnPowerUpActivated;
        public event Action<PowerUpType> OnPowerUpExpired;

        private readonly Dictionary<PowerUpType, IPowerUpEffect> _activeEffects = new Dictionary<PowerUpType, IPowerUpEffect>();

        public bool IsEffectActive(PowerUpType type)
        {
            return _activeEffects.TryGetValue(type, out var effect) && effect.IsActive;
        }

        public float GetRemainingTime(PowerUpType type)
        {
            return _activeEffects.TryGetValue(type, out var effect) ? effect.RemainingTime : 0f;
        }

        public void ApplyPowerUp(PowerUpDefinition definition)
        {
            if (definition == null) return;

            IPowerUpEffect effect = definition.powerUpType switch
            {
                PowerUpType.CoinMagnet => new CoinMagnetEffect(),
                PowerUpType.ScoreMultiplier => new ScoreMultiplierEffect(),
                PowerUpType.Jetpack => new JetpackEffect(),
                PowerUpType.HoverboardShield => new HoverboardShieldEffect(),
                _ => null
            };

            if (effect != null)
            {
                if (_activeEffects.TryGetValue(definition.powerUpType, out var existing))
                {
                    existing.Cancel();
                }

                _activeEffects[definition.powerUpType] = effect;
                effect.Apply(gameObject, definition);
                OnPowerUpActivated?.Invoke(definition.powerUpType, definition.duration);
            }
        }

        private void Update()
        {
            float dt = Time.deltaTime;
            var expired = new List<PowerUpType>();

            foreach (var kvp in _activeEffects)
            {
                kvp.Value.Update(dt);
                if (!kvp.Value.IsActive)
                {
                    expired.Add(kvp.Key);
                }
            }

            foreach (var type in expired)
            {
                _activeEffects.Remove(type);
                OnPowerUpExpired?.Invoke(type);
            }
        }

        public void CancelAll()
        {
            foreach (var kvp in _activeEffects)
            {
                kvp.Value.Cancel();
                OnPowerUpExpired?.Invoke(kvp.Key);
            }
            _activeEffects.Clear();
        }
    }
}
