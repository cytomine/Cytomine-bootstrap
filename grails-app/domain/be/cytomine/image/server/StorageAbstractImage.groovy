package be.cytomine.image.server

import be.cytomine.image.AbstractImage

class StorageAbstractImage {
    Storage storage
    AbstractImage abstractImage

    static mapping = {
        version false
    }

    static StorageAbstractImage link(Storage storage, AbstractImage abstractImage) {
        def storageAbstractImage = StorageAbstractImage.findByStorageAndAbstractImage(storage, abstractImage)
        if (!storageAbstractImage) {
            storageAbstractImage = new StorageAbstractImage()
            storage.addToStorageAbstractImages(storageAbstractImage)
            abstractImage.addToStorageAbstractImages(storageAbstractImage)
            storageAbstractImage.save(flush: true)
        }

    }

    static StorageAbstractImage unlink(Storage storage, AbstractImage abstractImage) {
        def storageAbstractImage = StorageAbstractImage.findByStorageAndAbstractImage(storage, abstractImage)
        if (!storageAbstractImage) {
            storageAbstractImage = new StorageAbstractImage()
            storage.removeFromStorageAbstractImages(storageAbstractImage)
            abstractImage.removeFromStorageAbstractImages(storageAbstractImage)
            storageAbstractImage.delete(flush: true)
        }
    }
}
