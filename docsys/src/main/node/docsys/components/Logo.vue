<template>
  <div class="Logo">
    <div class="Section Bar" />
    <div class="Side">
      <div class="Section Weight Small" />
      <div class="Section Weight Medium" />
      <div class="Section Weight" />
    </div>
    <div class="Middle"/>
    <div class="Side">
      <div class="Section Weight" />
      <div class="Section Weight Medium" />
      <div class="Section Weight Small" />
    </div>
  </div>
</template>

<style>

.Side{
    display: inline-block;
    position: relative;
    width: 15%;
}
.Middle{
    display: inline-block;
    position: relative;
    width: 65%;
}

.Logo {
  display: inline-block;
  animation: turn 2s linear forwards 1s;
  transform: rotateX(180deg);
  position: relative;
  overflow: hidden;
  height: 180px;
  width: 400px;
}

.Section{
  position: absolute;
  top: 0;
  left: 0;
  width: 0;
  height: 0;
}

.Weight{
  position: relative;
  display:inline-block;
  width: 14px;
  height: 90px;
  background-color: #0C1439;
  border-radius: 30% 30% 30% 30% / 10% 10% 10% 10% ;
}
.Medium{
  transform: scale(.7);
}
.Small{
  transform: scale(.4);
}

.Bar{
  width: 100%;
  height: 15px;
  background-color: #FF7D2B;
  margin-top: 37px;
}

@keyframes turn {
  100% {
    transform: rotateX(0deg);
  }
}

@keyframes godown {
  100% {
    top: 180px;
  }
}

@keyframes goright {
  100% {
    left: 70px;
  }
}
</style>
