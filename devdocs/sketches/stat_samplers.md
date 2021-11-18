This diagram shows the base implementations of all the statistical sampler
wrappers, the types they implement, and the helper functions which are key
to their operation.


```plantuml

digraph samplers {
 rankdir=LR;
 node[shape=box];

 subgraph cluster0 {
  label="continuous"
   subgraph cluster3 {
    label="int->double"
    IntToDoubleContinuousCurve[shape=box]
    IntToDoubleContinuousCurve -> IntToDoubleFunction[style=dashed]
    IntToDoubleContinuousCurve -> InterpolatingIntDoubleSampler
    IntToDoubleContinuousCurve -> RealIntDoubleSampler
   }
   subgraph cluster4 {
    label="long->double"
    LongToDoubleContinuousCurve[shape=box]
    LongToDoubleContinuousCurve -> LongToDoubleFunction[style=dashed]
    LongToDoubleContinuousCurve -> InterpolatingLongDoubleSampler
    LongToDoubleContinuousCurve -> RealLongDoubleSampler
   }
 }
 subgraph cluster1 {
  label="discrete"
  subgraph cluster5 {
   label="int->int"
   IntToIntDiscreteCurve[shape=box]
   IntToIntDiscreteCurve -> IntUnaryOperator[style=dashed]
   IntToIntDiscreteCurve -> InterpolatingIntIntSampler
   IntToIntDiscreteCurve -> DiscreteIntIntSampler
  }

  subgraph cluster6 {
   label="int->long"
   IntToLongDiscreteCurve[shape=box]
   IntToLongDiscreteCurve -> IntToLongFunction[style=dashed]
   IntToLongDiscreteCurve -> InterpolatingIntLongSampler
   IntToLongDiscreteCurve -> DiscreteIntLongSampler
  }

  subgraph cluster7 {
   label="long->int"
   LongToIntDiscreteCurve[shape=box]
   LongToIntDiscreteCurve -> LongToIntFunction[style=dashed]
   LongToIntDiscreteCurve ->InterpolatingLongIntSampler
   LongToIntDiscreteCurve ->DiscreteLongIntSampler
  }

  subgraph cluster8 {
   label="long->long"
   LongToLongDiscreteCurve[shape=box]
   LongToLongDiscreteCurve -> LongUnaryOperator[style=dashed]
   LongToLongDiscreteCurve ->InterpolatingLongLongSampler
   LongToLongDiscreteCurve ->DiscreteLongLongSampler
  }

 }
}
```
