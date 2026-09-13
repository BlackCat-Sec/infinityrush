namespace InfinityRush.Save
{
    public interface ISaveService
    {
        bool Save<T>(string key, T data);
        bool Load<T>(string key, out T data);
        bool HasKey(string key);
        void DeleteKey(string key);
        void ClearAll();
    }
}
