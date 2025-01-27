#  Copyright (c) 2025 nosqlbench
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#

import numpy as np
from sklearn.neighbors import NearestNeighbors
import h5py

def generate_knn_dataset(n, p, x, output_file):
    # Step 1: Generate 'train' data (n vectors of size p) with associated ids
    train_data = np.random.rand(n, p).astype(np.float32)
    train_ids = np.repeat(np.arange(1, n // 100 + 1), 100)  # Assign 100 contiguous vectors per id

    # Step 2: Generate 'test' data (x vectors of size p) with associated ids
    test_data = np.random.rand(x, p).astype(np.float32)
    test_ids = []
    for _ in range(x):
        num_ids = np.random.randint(1, 6)  # Each test query is associated with 1 to 5 training ids
        associated_ids = np.random.choice(np.arange(1, n // 100 + 1), size=num_ids, replace=False)
        test_ids.append(associated_ids)

    # Step 3: Compute KNN for 'test' data using 'train' data filtered by associated ids
    neighbors_list = []
    for i in range(x):
        query_vector = test_data[i]
        query_ids = test_ids[i]

        # Filter train data by matching ids
        mask = np.isin(train_ids, query_ids)
        filtered_train_data = train_data[mask]
        global_indices = np.where(mask)[0]  # Get global indices of the filtered train data

        knn = NearestNeighbors(n_neighbors=100, algorithm='auto')
        knn.fit(filtered_train_data)
        neighbors = knn.kneighbors(query_vector.reshape(1, -1), return_distance=False)

        # Map local indices back to global indices
        global_neighbors = global_indices[neighbors[0]]
        neighbors_list.append(global_neighbors)

    # Step 4: Write data to HDF5 file
    with h5py.File(output_file, 'w') as h5f:
        h5f.create_dataset('train', data=train_data)
        h5f.create_dataset('train_ids', data=train_ids)
        h5f.create_dataset('test', data=test_data)
        h5f.create_dataset('test_ids', data=np.array(test_ids, dtype=object), dtype=h5py.special_dtype(vlen=np.int32))
        h5f.create_dataset('neighbors', data=np.array(neighbors_list, dtype=np.int32))

    print(f"Dataset saved to {output_file}")

# Example usage
if __name__ == "__main__":
    n = int(input("Enter the number of train vectors (n): "))
    p = int(input("Enter the dimensionality of each vector (p): "))
    x = int(input("Enter the number of test vectors (x): "))
    output_file = input("Enter the output HDF5 file name: ")

    generate_knn_dataset(n, p, x, output_file)
