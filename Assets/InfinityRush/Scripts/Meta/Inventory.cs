using System;
using System.Collections.Generic;

namespace InfinityRush.Meta
{
    [Serializable]
    public class Inventory
    {
        public List<string> unlockedCharacterIds = new List<string>();
        public List<string> unlockedBoardIds = new List<string>();

        public void InitDefaults()
        {
            unlockedCharacterIds.Clear();
            unlockedCharacterIds.Add("char_default");

            unlockedBoardIds.Clear();
            unlockedBoardIds.Add("board_default");
        }

        public bool IsCharacterUnlocked(string id) => unlockedCharacterIds.Contains(id);
        public bool IsBoardUnlocked(string id) => unlockedBoardIds.Contains(id);

        public void UnlockCharacter(string id)
        {
            if (!IsCharacterUnlocked(id)) unlockedCharacterIds.Add(id);
        }

        public void UnlockBoard(string id)
        {
            if (!IsBoardUnlocked(id)) unlockedBoardIds.Add(id);
        }
    }
}
