# Sistema de Gestão de Eventos — Projeto de Programação N04 (Grupo 2)

Aplicação completa para cadastro, listagem e gerenciamento de eventos, composta por:
- Backend em Spring Boot (REST + H2 in-memory)
- Cliente desktop em JavaFX com interface moderna e tabs

Este README explica como o sistema funciona, como executar, como usar as APIs e quais são as regras de validação de negócio implementadas.

---

## Visão Geral

O sistema permite:
- Criar, editar, listar e excluir eventos
- Filtrar eventos por título, categoria e data de início
- Gerenciar categorias no backend e consumi-las no cliente
- Visualizar relatórios simples (contagem de eventos e vagas disponíveis)

Componentes principais:
- `EventoController` expõe as rotas REST em `/api/eventos`
- `EventoService` centraliza regras de negócio e validações
- Entidades: `Evento`, `Categoria`, `Local`, `Organizacao`, `Usuario`
- Cliente JavaFX (módulo `client-javafx`) com telas: Eventos, Categorias, Organizadores, Relatórios e Avaliação

---

## Arquitetura

- Backend: Spring Boot + JPA/Hibernate, banco H2 em memória (`jdbc:h2:mem:eventsdb`)
- Cliente: JavaFX 21 (FXML + CSS), comunicação HTTP usando um `ApiClient`
- Comunicação: JSON via REST
- Build: Maven em ambos os módulos

H2 Console:
- Disponível em `/h2-console`
- JDBC URL: `jdbc:h2:mem:eventsdb`
- Usuário: `SA` (sem senha)

---

## Regras de Negócio e Validações

- Data de início do evento deve ser no futuro
- Capacidade: `limiteVagas` não pode exceder `capacidadeMaxima` do `Local` (se `Local` for informado)
- Conflitos de agenda: verificados apenas quando há `Local` definido
- Permissão de organizador:
  - Se não houver organização, a criação/edição é permitida
  - Se houver organização sem `id` (apenas texto), é tratada como não informada
  - Se houver organização válida, o usuário do cabeçalho `Usuario-Id` precisa ter papel `ORGANIZADOR` na organização

Observações importantes:
- Campo `Local` é opcional (coluna `local_id` é `nullable`)
- O cliente JavaFX atualmente envia o campo “Organizador” como texto simples; no backend isso é desassociado se não houver `id`

---

## API — Eventos

Base URL: `http://localhost:8080`

Rotas principais (`EventoController`):
- `GET /api/eventos` — lista todos os eventos
- `GET /api/eventos/{id}` — busca por id
- `GET /api/eventos/busca?titulo=&categoriaId=&dataInicio=` — filtros (data no formato ISO-8601)
- `POST /api/eventos` — cria um evento (requer header `Usuario-Id`)
- `PUT /api/eventos/{id}` — atualiza (requer header `Usuario-Id`)
- `DELETE /api/eventos/{id}` — exclui (requer header `Usuario-Id`)

Headers:
- `Usuario-Id: <id_do_usuario>` — obrigatório em operações de escrita

Modelo de `Evento` (JSON de exemplo):
```json
{
  "titulo": "Festa de Programação",
  "descricao": "Evento para devs",
  "inicio": "2025-11-24T19:00:00",
  "fim": "2025-11-25T00:00:00",
  "limiteVagas": 60,
  "categoria": { "id": 1 },
  "organizacao": null,
  "local": null
}
```

Tratamento de erros (HTTP 400):
- `IllegalStateException` — violações de regras (data, capacidade, conflito)
- `PropertyValueException` / `DataIntegrityViolationException` — erros de integridade
- `TransientObjectException` — entidade relacionada sem `id` (ex.: organização só com texto)

---

## API — Categorias

Base: `/api/categorias`
- `GET /api/categorias` — lista categorias
- `POST /api/categorias` — cria categoria

Obs.: O cliente JavaFX consome categorias para preencher filtros e o formulário de evento.

---

## Executando o Backend

Pré-requisitos:
- Java 21
- Maven 3.8+

Build e execução:
```bash
mvn -DskipTests package
java -jar target/ProjetoProgamacao-0.0.1-SNAPSHOT.jar
```

Após iniciar:
- Tomcat em `http://localhost:8080`
- H2 Console em `http://localhost:8080/h2-console`

---

## Executando o Cliente JavaFX

Direto pelo Maven (recomendado):
```bash
mvn -pl client-javafx -am javafx:run
```

Ou pela IDE:
- Abra o módulo `client-javafx`
- Rode a classe `com.novaaurora.events.Main`

Tabs disponíveis no cliente:
- Eventos: listar, criar, editar, filtrar
- Categorias: criar e listar
- Organizadores: listar (depende do backend `/api/organizacoes` se disponível)
- Relatórios: estatísticas básicas
- Avaliação: placeholder

Formulário de Evento:
- Campos: Título, Descrição, Categoria, Data de Início, Data de Término, Limite de Vagas, Organizador (texto), Autorizado (checkbox)
- `Local` não é informado pelo cliente atualmente; o backend aceita `null`

---

## Dicas de Uso e Solução de Problemas

- 404 ao criar: verifique a URL usada pelo cliente e se o backend está em `:8080`
- `TransientObjectException`: ocorre se você enviar uma organização sem `id`; o backend já desassocia automaticamente na criação
- `Could not write JSON: Unimplemented method 'getPapel'`: corrigido removendo getter indevido em `Evento`
- Datas: use formato ISO-8601 (`YYYY-MM-DDTHH:MM:SS`)

---

## Desenvolvimento

Estrutura do repositório:
- `src/main/java/...` — código do backend (controllers, services, entities, repositories)
- `client-javafx/src/main/java/...` — código do cliente
- `client-javafx/src/main/resources/...` — FXML e CSS

Builds e testes:
- Backend: `mvn package`

- Cliente: `mvn -pl client-javafx -am javafx:run`
- Testes de UI (TestFX) estão desabilitados por padrão e exigem ambiente headless configurado

Estilo e segurança:
- Validações no `EventoService`
- Handlers de exceção no `EventoController` para respostas amigáveis ao cliente

---
## Diagrama de Classes:

![diagrama de classes](https://github.com/user-attachments/assets/854576f5-dae2-4b07-9e54-cccba700463d)

## Demonstração com print do funcionamento do sistema

### Tela inicial

![Captura de tela 2025-11-17 213824](https://github.com/user-attachments/assets/b35ae202-e275-4b52-b051-4400f7f74c16)

### Criação de um Evento

![Captura de tela 2025-11-17 214011](https://github.com/user-attachments/assets/cbc79f7f-efc0-4c13-841e-6bc21eb05cae)

#### Evento criado e adcionado na tela inicial

![Captura de tela 2025-11-17 213952](https://github.com/user-attachments/assets/d75f5d7f-8532-4c25-85bf-343b185f4b79)


### Editar um evento

![Captura de tela 2025-11-17 214044](https://github.com/user-attachments/assets/38442dbc-d81b-4e85-aa99-ee9f7b40374f)

### Excluir um evento

![Captura de tela 2025-11-17 214105](https://github.com/user-attachments/assets/6b304e03-1823-4011-b007-9e47db1dac25)

### Avaliação de eventos

![Captura de tela 2025-11-17 214129](https://github.com/user-attachments/assets/2233a083-5c2b-4730-a261-470e0fdf1dbf)

### Relatórios

![Captura de tela 2025-11-17 214142](https://github.com/user-attachments/assets/e769461e-4b43-44e3-8c52-b997854d5293)

### categorias

![Captura de tela 2025-11-17 214152](https://github.com/user-attachments/assets/ff9e13dd-221b-4a46-8af0-a24cf42921c0)


## Equipe

● Cauã De Souza Rodrigues  
● João Augusto Jacinto Gonçalves  
● Reinaldo Henrique  
● Vitor Lima Prudente  
● Ricardo Barbosa Santos Filho
