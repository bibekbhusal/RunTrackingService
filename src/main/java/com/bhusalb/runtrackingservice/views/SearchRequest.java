package com.bhusalb.runtrackingservice.views;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchRequest<T extends SearchQuery> {

    @Valid
    @NotNull
    private Page page;

    @Valid
    @NotNull
    private T query;

    public SearchRequest (final T query) {
        this.page = new Page();
        this.query = query;
    }
}
