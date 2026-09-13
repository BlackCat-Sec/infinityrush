using System.Collections.Generic;
using UnityEngine;
using InfinityRush.Core;
using InfinityRush.Pooling;

namespace InfinityRush.Generation
{
    public class TrackDirector : MonoBehaviour
    {
        [Header("Configurations")]
        [SerializeField] private DifficultyDirector difficultyDirector;
        [SerializeField] private TrackSegment segmentPrefab;
        [SerializeField] private List<TrackPatternDefinition> patternPool = new List<TrackPatternDefinition>();

        [Header("Parameters")]
        [SerializeField] private int activeSegmentCount = 6;
        [SerializeField] private float segmentLength = 30.0f;

        private readonly Queue<TrackSegment> _activeSegments = new Queue<TrackSegment>();
        private PatternSelector _patternSelector;
        private RunRandom _random;
        private float _nextSpawnZ = 0f;

        private void Awake()
        {
            if (difficultyDirector == null)
            {
                difficultyDirector = GetComponent<DifficultyDirector>() ?? gameObject.AddComponent<DifficultyDirector>();
            }

            _random = new RunRandom(12345);
            _patternSelector = new PatternSelector(patternPool);

            ServiceRegistry.Instance.Register(this);
        }

        private void Start()
        {
            InitTrack();
        }

        public void InitTrack()
        {
            ClearSegments();
            _nextSpawnZ = 0f;

            for (int i = 0; i < activeSegmentCount; i++)
            {
                SpawnNextSegment();
            }
        }

        private void Update()
        {
            float dt = Time.deltaTime;
            difficultyDirector.UpdateDifficulty(dt);
            float speed = difficultyDirector.CurrentWorldSpeed;

            Vector3 move = new Vector3(0f, 0f, -speed * dt);

            foreach (var seg in _activeSegments)
            {
                seg.transform.position += move;
            }

            _nextSpawnZ -= speed * dt;

            if (_activeSegments.Count > 0)
            {
                var first = _activeSegments.Peek();
                if (first.transform.position.z + segmentLength < -20.0f)
                {
                    DespawnOldestSegment();
                    SpawnNextSegment();
                }
            }
        }

        private void SpawnNextSegment()
        {
            Vector3 pos = new Vector3(0f, 0f, _nextSpawnZ);

            TrackSegment seg;
            if (ServiceRegistry.Instance.TryGet<IPoolService>(out var poolService) && segmentPrefab != null)
            {
                seg = poolService.Get(segmentPrefab, pos, Quaternion.identity);
            }
            else if (segmentPrefab != null)
            {
                seg = Instantiate(segmentPrefab, pos, Quaternion.identity, transform);
            }
            else
            {
                var go = GameObject.CreatePrimitive(PrimitiveType.Cube);
                go.name = "SegmentPlaceholder";
                go.transform.position = pos;
                go.transform.localScale = new Vector3(8.0f, 0.2f, segmentLength);
                go.transform.SetParent(transform);
                seg = go.AddComponent<TrackSegment>();
                seg.SetLength(segmentLength);
            }

            _activeSegments.Enqueue(seg);
            _nextSpawnZ += segmentLength;
        }

        private void DespawnOldestSegment()
        {
            if (_activeSegments.Count == 0) return;

            var oldSeg = _activeSegments.Dequeue();
            if (ServiceRegistry.Instance.TryGet<IPoolService>(out var poolService))
            {
                poolService.Release(oldSeg);
            }
            else
            {
                Destroy(oldSeg.gameObject);
            }
        }

        private void ClearSegments()
        {
            while (_activeSegments.Count > 0)
            {
                DespawnOldestSegment();
            }
        }

        private void OnDestroy()
        {
            ServiceRegistry.Instance.Unregister<TrackDirector>();
        }
    }
}
