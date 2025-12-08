package com.fileshare.app.data.repository

import com.fileshare.app.data.local.dao.DocumentDao
import com.fileshare.app.data.local.dao.DocumentImageDao
import com.fileshare.app.data.local.entity.DocumentEntity
import com.fileshare.app.data.local.entity.DocumentImageEntity
import com.fileshare.app.domain.model.Document
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine

class DocumentRepository(
    private val documentDao: DocumentDao,
    private val documentImageDao: DocumentImageDao
) {
    
    fun getAllDocuments(): Flow<List<Document>> {
        return combine(
            documentDao.getAllDocuments(),
            documentImageDao.getAllImages()
        ) { documents, allImages ->
            documents.map { doc ->
                val images = allImages.filter { it.documentId == doc.id }
                    .sortedBy { it.displayOrder }
                doc.toDomain(images.map { it.imageUri })
            }
        }
    }
    
    fun getDocumentsByCategory(categoryId: Long): Flow<List<Document>> {
        return combine(
            documentDao.getDocumentsByCategory(categoryId),
            documentImageDao.getAllImages()
        ) { documents, allImages ->
            documents.map { doc ->
                val images = allImages.filter { it.documentId == doc.id }
                    .sortedBy { it.displayOrder }
                doc.toDomain(images.map { it.imageUri })
            }
        }
    }
    
    suspend fun getDocumentById(id: Long): Document? {
        val entity = documentDao.getDocumentById(id) ?: return null
        val images = documentImageDao.getImagesByDocumentIdSync(id)
        return entity.toDomain(images.map { it.imageUri })
    }
    
    fun getDocumentByIdFlow(id: Long): Flow<Document?> {
        return combine(
            documentDao.getDocumentByIdFlow(id),
            documentImageDao.getImagesByDocumentId(id)
        ) { document, images ->
            document?.toDomain(images.map { it.imageUri })
        }
    }
    
    suspend fun insertDocument(document: Document, fileTypes: List<String> = emptyList()): Long {
        val entity = DocumentEntity.fromDomain(document)
        val documentId = documentDao.insertDocument(entity)
        
        // Insert images/PDFs
        document.imageUris.forEachIndexed { index, uri ->
            val fileType = fileTypes.getOrNull(index) ?: "IMAGE"
            val imageEntity = DocumentImageEntity(
                documentId = documentId,
                imageUri = uri,
                fileType = fileType,
                displayOrder = index,
                createdAt = System.currentTimeMillis()
            )
            documentImageDao.insertImage(imageEntity)
        }
        
        return documentId
    }
    
    suspend fun updateDocument(document: Document, fileTypes: List<String> = emptyList()) {
        val entity = DocumentEntity.fromDomain(document)
        documentDao.updateDocument(entity)
        
        // Delete existing images and insert new ones
        documentImageDao.deleteImagesByDocumentId(document.id)
        document.imageUris.forEachIndexed { index, uri ->
            val fileType = fileTypes.getOrNull(index) ?: "IMAGE"
            val imageEntity = DocumentImageEntity(
                documentId = document.id,
                imageUri = uri,
                fileType = fileType,
                displayOrder = index,
                createdAt = System.currentTimeMillis()
            )
            documentImageDao.insertImage(imageEntity)
        }
    }
    
    suspend fun deleteDocument(document: Document) {
        val entity = DocumentEntity.fromDomain(document)
        documentDao.deleteDocument(entity)
        // Images will be auto-deleted due to CASCADE
    }
    
    suspend fun deleteDocumentById(id: Long) {
        documentDao.deleteDocumentById(id)
        // Images will be auto-deleted due to CASCADE
    }
    
    suspend fun getDocumentCount(): Int {
        return documentDao.getDocumentCount()
    }
    
    suspend fun incrementShareCount(id: Long) {
        documentDao.incrementShareCount(id)
    }
}
