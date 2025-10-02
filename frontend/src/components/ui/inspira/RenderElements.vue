<template>
  <template v-for="el in elements" :key="String(prefix + '/' + el.name)">
    <Folder v-if="el.directory" :id="String(prefix + '/' + el.name)" :name="el.name">
      <RenderElements :elements="el.children || []" :prefix="String(prefix + '/' + el.name)" />
    </Folder>
    <File v-else :id="String(prefix + '/' + el.name)" :name="el.name" />
  </template>
  
</template>

<script setup lang="ts">
import Folder from './Folder.vue'
import File from './File.vue'

interface Node { name: string; directory?: boolean; children?: Node[] }

defineProps<{ elements: Node[]; prefix: string }>()
</script>

<style scoped>
</style>

