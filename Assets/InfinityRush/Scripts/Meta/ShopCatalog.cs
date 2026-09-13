using System.Collections.Generic;
using UnityEngine;

namespace InfinityRush.Meta
{
    [CreateAssetMenu(fileName = "ShopCatalog", menuName = "InfinityRush/Shop Catalog")]
    public class ShopCatalog : ScriptableObject
    {
        public List<CharacterDefinition> characters = new List<CharacterDefinition>();
        public List<BoardDefinition> boards = new List<BoardDefinition>();
    }
}
