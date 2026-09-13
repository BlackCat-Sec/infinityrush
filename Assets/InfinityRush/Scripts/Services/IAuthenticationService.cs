using System;

namespace InfinityRush.Services
{
    public interface IAuthenticationService
    {
        bool IsSignedIn { get; }
        string PlayerId { get; }

        void SignInAnonymously(Action<bool, string> onComplete);
    }
}
