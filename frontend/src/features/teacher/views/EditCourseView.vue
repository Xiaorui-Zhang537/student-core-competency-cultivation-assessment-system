<template>
  <div class="p-6 bg-gray-50">
    <div class="max-w-4xl mx-auto">
      <!-- Header -->
      <div class="mb-8">
        <PageHeader :title="t('teacher.courseEdit.title')" :subtitle="t('teacher.courseEdit.subtitle')" />
      </div>

      <!-- Loading State -->
      <div v-if="courseStore.loading && !courseForm.id" class="text-center py-12 card">
        <p>{{ t('teacher.courseEdit.loading') }}</p>
      </div>

      <!-- Form -->
      <form v-else-if="courseForm.id" @submit.prevent="handleUpdateCourse" class="card p-8 space-y-6">
                <div>
          <label for="title" class="label">{{ t('teacher.courseEdit.form.title') }} <span class="text-red-500">*</span></label>
          <GlassInput id="title" v-model="courseForm.title" type="text" required />
                </div>

                <div>
          <label for="description" class="label">{{ t('teacher.courseEdit.form.description') }} <span class="text-red-500">*</span></label>
          <GlassTextarea id="description" v-model="courseForm.description" :rows="3" required />
                </div>

                <div>
          <label for="content" class="label">{{ t('teacher.courseEdit.form.content') }}</label>
          <GlassTextarea id="content" v-model="courseForm.content" :rows="6" />
                  </div>

                  <div>
          <label for="category" class="label">{{ t('teacher.courseEdit.form.category') }} <span class="text-red-500">*</span></label>
          <select id="category" v-model="courseForm.category" class="ui-pill--select ui-pill--pl ui-pill--md ui-pill--pr-select" required>
            <option disabled value="">{{ t('teacher.courseEdit.form.selectCategory') }}</option>
            <option v-for="cat in categories" :key="cat" :value="cat">{{ cat }}</option>
                    </select>
                </div>

        <!-- Actions -->
        <div class="flex justify-end gap-4 pt-4">
            <router-link :to="'/teacher/courses/' + courseForm.id">
              <Button variant="secondary">{{ t('teacher.courseEdit.actions.cancel') }}</Button>
            </router-link>
            <Button type="submit" variant="primary" :disabled="courseStore.loading">
                {{ courseStore.loading ? t('teacher.courseEdit.actions.saving') : t('teacher.courseEdit.actions.save') }}
            </Button>
        </div>
      </form>
      
      <!-- Error State -->
      <div v-else class="text-center py-12 card">
          <h3 class="text-lg font-medium">{{ t('teacher.courseEdit.error.title') }}</h3>
          <p class="text-gray-500 mt-2">{{ t('teacher.courseEdit.error.desc') }}</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useCourseStore } from '@/stores/course';
import type { CourseUpdateRequest } from '@/types/course';
import { useI18n } from 'vue-i18n'
import Button from '@/components/ui/Button.vue'
import PageHeader from '@/components/ui/PageHeader.vue'
import GlassInput from '@/components/ui/inputs/GlassInput.vue'
import GlassTextarea from '@/components/ui/inputs/GlassTextarea.vue'

const route = useRoute();
const router = useRouter();
const courseStore = useCourseStore();
const { t } = useI18n()

const courseForm = reactive<CourseUpdateRequest & { id: string | null }>({
  id: null,
  title: '',
  description: '',
  content: '',
  category: '',
});

// Mock categories
// reuse t from useI18n to avoid redeclare
const categories = [
  t('teacher.courseEdit.form.categoryOptions.programming'),
  t('teacher.courseEdit.form.categoryOptions.design'),
  t('teacher.courseEdit.form.categoryOptions.business'),
  t('teacher.courseEdit.form.categoryOptions.marketing'),
  t('teacher.courseEdit.form.categoryOptions.language'),
  t('teacher.courseEdit.form.categoryOptions.science'),
  t('teacher.courseEdit.form.categoryOptions.art'),
  t('teacher.courseEdit.form.categoryOptions.other'),
];

const handleUpdateCourse = async () => {
    if (!courseForm.id) return;
    
    const { id, ...updateData } = courseForm;

    const response = await courseStore.updateCourse(courseForm.id, updateData);
    if (response) {
        router.push(`/teacher/courses/${courseForm.id}`);
    }
};

// Watch for the course data to be loaded into the store
watch(() => courseStore.currentCourse, (newCourse) => {
    if (newCourse) {
        courseForm.id = String(newCourse.id);
        courseForm.title = newCourse.title;
        courseForm.description = newCourse.description;
        courseForm.content = newCourse.content || '';
        courseForm.category = newCourse.category;
    }
});

onMounted(async () => {
  const courseId = route.params.id as string;
  if (courseId) {
    await courseStore.fetchCourseById(courseId);
  } else {
      // Handle case where ID is missing
      router.push('/teacher/manage-course');
  }
});
</script>

<style scoped lang="postcss">
.label {
    @apply block text-sm font-medium text-gray-700 mb-2;
}
</style>
