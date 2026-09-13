using System;
using System.Collections.Generic;
using UnityEngine;
using InfinityRush.Core;

namespace InfinityRush.Pooling
{
    public interface IPoolService
    {
        T Get<T>(T prefab, Vector3 position, Quaternion rotation) where T : Component;
        void Release<T>(T instance) where T : Component;
    }

    public class PoolService : MonoBehaviour, IPoolService
    {
        private readonly Dictionary<int, Queue<Component>> _pools = new Dictionary<int, Queue<Component>>();
        private readonly Dictionary<int, int> _instanceToPrefabMap = new Dictionary<int, int>();

        private void Awake()
        {
            ServiceRegistry.Instance.Register<IPoolService>(this);
        }

        public T Get<T>(T prefab, Vector3 position, Quaternion rotation) where T : Component
        {
            if (prefab == null) throw new ArgumentNullException(nameof(prefab));

            int prefabId = prefab.gameObject.GetInstanceID();

            if (!_pools.TryGetValue(prefabId, out var queue))
            {
                queue = new Queue<Component>();
                _pools[prefabId] = queue;
            }

            T instance;
            if (queue.Count > 0)
            {
                instance = (T)queue.Dequeue();
                instance.transform.SetPositionAndRotation(position, rotation);
                instance.gameObject.SetActive(true);
            }
            else
            {
                instance = Instantiate(prefab, position, rotation);
            }

            int instanceId = instance.gameObject.GetInstanceID();
            _instanceToPrefabMap[instanceId] = prefabId;

            return instance;
        }

        public void Release<T>(T instance) where T : Component
        {
            if (instance == null) return;

            int instanceId = instance.gameObject.GetInstanceID();
            if (_instanceToPrefabMap.TryGetValue(instanceId, out int prefabId))
            {
                instance.gameObject.SetActive(false);
                if (_pools.TryGetValue(prefabId, out var queue))
                {
                    queue.Enqueue(instance);
                }
            }
            else
            {
                Destroy(instance.gameObject);
            }
        }

        private void OnDestroy()
        {
            ServiceRegistry.Instance.Unregister<IPoolService>();
        }
    }
}
