using UnityEngine;

namespace InfinityRush.Pooling
{
    public interface IPool<T> where T : Component
    {
        T Get();
        void Release(T item);
        void Clear();
    }
}
