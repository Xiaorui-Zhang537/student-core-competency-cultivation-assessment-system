<template>
  <div class="absolute inset-0 overflow-hidden">
    <div class="aurora a1"></div>
    <div class="aurora a2"></div>
    <div class="aurora a3"></div>
  </div>
</template>

<script setup lang="ts">
</script>

<style scoped>
.aurora {
  position: absolute;
  filter: blur(60px);
  opacity: 0.35;
  mix-blend-mode: screen;
}
.a1 { top: -10%; left: -10%; width: 60%; height: 60%; background: radial-gradient(circle at 30% 30%, #7dd3fc, transparent 60%); animation: float1 18s ease-in-out infinite; }
.a2 { bottom: -15%; right: -10%; width: 65%; height: 65%; background: radial-gradient(circle at 70% 40%, #f0abfc, transparent 60%); animation: float2 22s ease-in-out infinite; }
.a3 { top: 20%; right: 10%; width: 50%; height: 50%; background: radial-gradient(circle at 40% 60%, #bef264, transparent 60%); animation: float3 26s ease-in-out infinite; }

@keyframes float1 { 0%,100%{ transform: translate3d(0,0,0)} 50%{ transform: translate3d(10px, -20px, 0)} }
@keyframes float2 { 0%,100%{ transform: translate3d(0,0,0)} 50%{ transform: translate3d(-12px, 18px, 0)} }
@keyframes float3 { 0%,100%{ transform: translate3d(0,0,0)} 50%{ transform: translate3d(14px, -12px, 0)} }
</style>


