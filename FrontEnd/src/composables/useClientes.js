// Compatibilidade para consumidores antigos; o estado pertence ao Pinia.
import { storeToRefs } from 'pinia'
import { useUploadStore } from '../stores/uploadStore.js'

export function useClientes() {
  const store = useUploadStore()
  return { ...storeToRefs(store), importarPlanilha: store.importarPlanilha, limpar: store.limpar }
}
