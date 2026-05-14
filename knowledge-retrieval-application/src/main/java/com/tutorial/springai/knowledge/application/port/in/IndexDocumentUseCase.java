package com.tutorial.springai.knowledge.application.port.in;

import com.tutorial.springai.knowledge.domain.model.DocumentId;

public interface IndexDocumentUseCase {

    DocumentId index(IndexDocumentCommand command);
}
