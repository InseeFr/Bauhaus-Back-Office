package fr.insee.rmes.domain.codeslist;

import fr.insee.rmes.domain.codeslist.model.CodesListDomain;

import java.util.List;

/**
 * Domain implementation of CodesListService.
 * Pure business logic without framework dependencies.
 */
public class CodesListServiceImpl implements CodesListService {
    
    private final CodesListRepository codesListRepository;
    
    public CodesListServiceImpl(CodesListRepository codesListRepository) {
        this.codesListRepository = codesListRepository;
    }
    
    @Override
    public List<CodesListDomain> getAllCodesLists(boolean partial) {
        return codesListRepository.findAllCodesLists(partial);
    }
}