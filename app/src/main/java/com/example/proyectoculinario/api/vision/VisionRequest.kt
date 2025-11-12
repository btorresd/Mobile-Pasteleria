
package com.example.proyectoculinario.api.vision

data class ImageContent(
    val content: String
)

data class Feature(
    val type: String = "LABEL_DETECTION",
    val maxResults: Int = 5
)

data class AnnotateImageRequest(
    val image: ImageContent,
    val features: List<Feature>
)

data class VisionRequest(
    val requests: List<AnnotateImageRequest>
)
