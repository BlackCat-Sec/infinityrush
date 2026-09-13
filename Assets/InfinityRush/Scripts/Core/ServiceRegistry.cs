using System;
using System.Collections.Generic;

namespace InfinityRush.Core
{
    public class ServiceRegistry
    {
        private static ServiceRegistry _instance;
        public static ServiceRegistry Instance => _instance ??= new ServiceRegistry();

        private readonly Dictionary<Type, object> _services = new Dictionary<Type, object>();

        public static void ResetInstance()
        {
            _instance = null;
        }

        public void Register<T>(T service) where T : class
        {
            if (service == null)
                throw new ArgumentNullException(nameof(service));

            var type = typeof(T);
            _services[type] = service;
        }

        public T Get<T>() where T : class
        {
            var type = typeof(T);
            if (_services.TryGetValue(type, out var service))
            {
                return (T)service;
            }
            throw new KeyNotFoundException($"Service of type {type.Name} is not registered.");
        }

        public bool TryGet<T>(out T service) where T : class
        {
            var type = typeof(T);
            if (_services.TryGetValue(type, out var obj))
            {
                service = (T)obj;
                return true;
            }

            service = null;
            return false;
        }

        public void Unregister<T>() where T : class
        {
            var type = typeof(T);
            _services.Remove(type);
        }

        public void Clear()
        {
            _services.Clear();
        }
    }
}
