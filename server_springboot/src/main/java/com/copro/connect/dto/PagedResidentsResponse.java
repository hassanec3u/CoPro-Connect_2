package com.copro.connect.dto;

import com.copro.connect.model.Resident;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagedResidentsResponse {
    
    private List<Resident> residents;
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private int pageSize;
    
}
