/*
 * Copyright (c) 2026 The NoSQLBench Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
//! Deterministic little-endian xvec fixture generator. It uses only Rust's
//! standard library so fixture regeneration does not require `links/`.
use std::{env, fs, io::Write, path::Path};

fn fvec(path: &Path, values: &[[f32; 2]]) {
    let mut file = fs::File::create(path).unwrap();
    for value in values { file.write_all(&(2i32).to_le_bytes()).unwrap(); for element in value { file.write_all(&element.to_le_bytes()).unwrap(); } }
}
fn ivec(path: &Path, values: &[[i32; 2]]) {
    let mut file = fs::File::create(path).unwrap();
    for value in values { file.write_all(&(2i32).to_le_bytes()).unwrap(); for element in value { file.write_all(&element.to_le_bytes()).unwrap(); } }
}
fn main() {
    let root = env::args().nth(1).expect("usage: generate_fixture <resource-dir>");
    let root = Path::new(&root); let demo = root.join("demo"); fs::create_dir_all(&demo).unwrap();
    fvec(&demo.join("base.fvec"), &[[1.0, 2.0], [3.0, 4.0]]);
    fvec(&demo.join("query.fvec"), &[[5.0, 6.0]]);
    ivec(&demo.join("neighbors.ivecs"), &[[7, 8]]);
    fs::write(demo.join("dataset.yaml"), "name: rust-v1-demo\nprofiles:\n  default:\n    base: base.fvec\n    query: query.fvec\n    neighbor_indices: neighbors.ivecs\n").unwrap();
    fs::write(root.join("catalog.yaml"), "datasets:\n  - name: rust-v1-demo\n    path: demo/dataset.yaml\n    dataset_type: dataset.yaml\n").unwrap();
    fs::write(root.join("fixture-manifest.yaml"), "producer: rustc standard-library fixture generator\nrustc: 1.94.1\nvectordata_rs_baseline: 1.7.1\nsource_commit: 1249310078785dbb59444f1c9bac14247767c286\nsha256:\n  catalog.yaml: d62e7c9c290d625661cd456f1c63714c9690819ade9798f75e3aee711a925834\n  demo/dataset.yaml: a7373d7a9615437a86ce15e31a7a83d0c3fd7c5fea2b1bff373ae4daeea64291\n  demo/base.fvec: e23a3d20150b1a9a4823424e84c3f0aa37750c30126a31582463d45a229b073d\n  demo/query.fvec: 2a7506bdf86a20a3cc2d5de9290dedac6ad2bafaf2342659030bb5785af9be69\n  demo/neighbors.ivecs: 9575df8994e60d8ce0930d937f8ec9503a8d231fcea1cc500c246fb0f6679839\n").unwrap();
}
