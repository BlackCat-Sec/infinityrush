using System;

namespace InfinityRush.Meta
{
    [Serializable]
    public class PlayerProfile
    {
        public string playerId;
        public int highScore;
        public int totalCoins;
        public string activeCharacterId;
        public string activeBoardId;

        public Wallet wallet;
        public Inventory inventory;

        public void InitDefaults()
        {
            playerId = Guid.NewGuid().ToString();
            highScore = 0;
            totalCoins = 0;
            activeCharacterId = "char_default";
            activeBoardId = "board_default";

            wallet = new Wallet();
            inventory = new Inventory();
            inventory.InitDefaults();
        }
    }
}
