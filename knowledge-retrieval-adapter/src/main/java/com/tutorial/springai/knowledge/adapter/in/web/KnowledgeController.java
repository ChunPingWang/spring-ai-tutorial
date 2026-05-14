package com.tutorial.springai.knowledge.adapter.in.web;

import com.tutorial.springai.knowledge.application.port.in.IndexDocumentCommand;
import com.tutorial.springai.knowledge.application.port.in.IndexDocumentUseCase;
import com.tutorial.springai.knowledge.application.port.in.RetrieveContextQuery;
import com.tutorial.springai.knowledge.application.port.in.RetrieveContextUseCase;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeController {

    private static final int DEFAULT_TOP_K = 3;

    private final IndexDocumentUseCase indexDocument;
    private final RetrieveContextUseCase retrieveContext;

    public KnowledgeController(IndexDocumentUseCase indexDocument, RetrieveContextUseCase retrieveContext) {
        this.indexDocument = indexDocument;
        this.retrieveContext = retrieveContext;
    }

    @PostMapping("/documents")
    public IndexDocumentResponse index(@RequestBody IndexDocumentRequest request) {
        var id = indexDocument.index(new IndexDocumentCommand(
                request.title(),
                request.source(),
                request.content()
        ));
        return new IndexDocumentResponse(id.asString());
    }

    @PostMapping("/queries")
    public QueryResponse retrieve(@RequestBody QueryRequest request) {
        int topK = request.topK() != null && request.topK() > 0 ? request.topK() : DEFAULT_TOP_K;
        var result = retrieveContext.retrieve(new RetrieveContextQuery(request.question(), topK));
        var hits = result.chunks().stream()
                .map(c -> new QueryResponse.Hit(c.content(), c.documentId(), c.score()))
                .toList();
        return new QueryResponse(hits);
    }
}
