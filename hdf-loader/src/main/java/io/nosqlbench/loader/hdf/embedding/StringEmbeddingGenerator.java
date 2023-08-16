/*
 * Copyright (c) 2023 nosqlbench
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

package io.nosqlbench.loader.hdf.embedding;

import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.text.sentenceiterator.BasicLineIterator;
import org.deeplearning4j.text.sentenceiterator.CollectionSentenceIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;

import java.util.Arrays;
import java.util.Collections;

public class StringEmbeddingGenerator implements EmbeddingGenerator {
    private final TokenizerFactory tokenizerFactory= new DefaultTokenizerFactory();

        @Override
        public float[][] generateEmbeddingFrom(Object o, int[] dims) {
            switch (dims.length) {
                case 1 -> {
                    return generateWordEmbeddings((String[]) o);
                }
                default -> throw new RuntimeException("unsupported embedding dimensionality: " + dims.length);
            }

        }

    private float[][] generateWordEmbeddings(String[] text) {
        SentenceIterator iter = new CollectionSentenceIterator(Collections.singletonList(text));
        /*Word2Vec vec = new Word2Vec.Builder()
            .minWordFrequency(1)
            .iterations(1)
            .layerSize(targetDims)
            .seed(42)
            .windowSize(5)
            .iterate(iter)
            .tokenizerFactory(tokenizerFactory)
            .build();
*/
        return null;
    }
}
