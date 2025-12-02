# Bauhaus-Back-Office
[![CII Best Practices](https://bestpractices.coreinfrastructure.org/projects/3795/badge)](https://bestpractices.coreinfrastructure.org/projects/3795)

Rest Endpoints and services Integration used by [Bauhaus](https://github.com/InseeFr/Bauhaus)

[![Build Status](https://travis-ci.org/InseeFr/Bauhaus-Back-Office.svg?branch=master)](https://travis-ci.org/InseeFr/Bauhaus-Back-Office)

The documentation can be found in the [docs](https://github.com/InseeFr/Bauhaus-Back-Office/tree/master/docs) folder and [browsed online](https://inseefr.github.io/Bauhaus-Back-Office).

## Gitleaks

We have set up Gitleaks on the project with a Git `pre-commit` hook.

To make it effective, you need to run `mvn install -DskipTests` beforehand.

Roadmap
- Ajouter un tests dans ArchUnit pour Interdire les @ControllerAdvice ? 
  - garder que des throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage(), e);
- Revoir les propriete et utiliser des @ConfigurationProperties et des @Values si pas besoin de beaucoup de choses
- Minimiser au maximum le commons -
  - shared-kernel -> domain commun a toute l'application : model, les ports
  - commons - pour les webservice "technique", et les infrastructure
- Demander a l'equipe si le swagger est encore utile ? 
  - Si non, on vire et on migre vers des endpoints Insomnia
  - Si ouim mettre les @ApiResponse locallement a la methode
- On Migre un module
  - On ecrit les tests comme @CollectionsEndToEndTest
  - On range les classes dans le package domain (pour s'assurer que ArchUnit plante)
  - On active arch unit pour ce nouveau module et on freez les resultats
  - Se poser la questions sur chaque fonctionanliteé, ce qui va dans le domaine, dans l'infra, ...
  - Creation des objets metier 
    - Utiliser des termes metier
    - Les Exceptions Metier
  - On Restructure le code - Creation les ports, adapteurs, 
    - Mettre en coherence entre le bouton Publier et les endpoints /publish
    - Mettre en coherence les endpoints des API /concepts/:id/publish /operations/publish/:id
  - Se poser la questions de nouvelles regles de validation metier et creer ticket Github pour les implementer ensuite. (a la creation d'une collection, valider les ID des concepts)
  - TU  
  - Revoir le Front 


- Concepts- Collections
- Organisations
- Operatoins Famille
- Nomenclature - Nomenclature
- Nomenclature Famille
- Nomenclature Series
- Operatoins Documents
- Operatoins Serie
- Operatoins Operation
- Operatoins Indicator
- Operatoins - Sims
- Concepts - Concepts
- Codes List
- Dataset et Themes
- Distribution
- Structure
- Component

- Sanctuariser une demi journee de travail en mob programming - 14h 16h 





- ModuleConfiguration pour la Configuration de Spring Boot pour un Module 
- ModuleProperties @ConfigurationProperties