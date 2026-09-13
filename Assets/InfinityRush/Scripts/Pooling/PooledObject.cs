using System;
using UnityEngine;

namespace InfinityRush.Pooling
{
    public class PooledObject : MonoBehaviour
    {
        public event Action<PooledObject> OnDespawn;

        public void Release()
        {
            OnDespawn?.Invoke(this);
            gameObject.SetActive(false);
        }
    }
}
