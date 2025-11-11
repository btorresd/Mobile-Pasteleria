package com.example.proyectoculinario.api.vision

data class LabelAnnotation(
    val description: String?,
    val score: Float?
)

data class AnnotateImageResponse(
    val labelAnnotations: List<LabelAnnotation>?
)

data class VisionResponse(
    val responses: List<AnnotateImageResponse>
)
