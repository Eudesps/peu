# Plataforma de Organização Acadêmica

> **Status:** proposta inicial do produto. O escopo descrito aqui ainda precisa ser validado com estudantes antes de orientar a implementação.

## 1. Objetivo do sistema

Oferecer a estudantes um espaço web para organizar atividades acadêmicas individuais e em grupo, acompanhar prazos e dividir trabalhos em subtarefas com responsáveis e progresso visíveis para as pessoas autorizadas.

O sistema deve ajudar cada estudante a entender o que precisa ser feito, até quando e por quem, preservando a responsabilidade principal de quem criou uma tarefa compartilhada.

## 2. Problema que pretende resolver

### Hipótese do problema

Estudantes podem organizar compromissos acadêmicos usando várias ferramentas — por exemplo, calendários, listas pessoais, planilhas e conversas em grupo. Essa dispersão pode dificultar a visualização dos prazos, a divisão de trabalhos e o acompanhamento de responsabilidades, contribuindo para tarefas esquecidas ou trabalho mal distribuído.

Essa descrição é uma hipótese de produto, ainda não uma conclusão de pesquisa. Precisamos confirmar com estudantes se o problema ocorre com frequência, quais ferramentas já usam e o que realmente sentem falta nelas.

## 3. Perfis de usuários

Os perfis abaixo são papéis dentro do produto, não necessariamente tipos diferentes de conta.

### Estudante responsável pela tarefa

Cria uma atividade individual ou compartilhada. Em uma tarefa compartilhada, é o responsável principal e administra seus dados centrais, participantes e subtarefas.

### Estudante participante

Tem acesso a uma tarefa compartilhada da qual participa. Pode visualizar seus dados e atualizar o progresso das subtarefas que lhe foram atribuídas.

### Estudante com tarefa individual

Organiza uma atividade própria. A proposta inicial é que somente seu criador possa acessá-la; essa regra de privacidade deve ser confirmada com estudantes.

**Fora dos perfis iniciais:** professores, coordenadores, administradores institucionais e pessoas sem conta não têm papéis definidos para o MVP.

## 4. Princípios de acesso

As permissões devem ser verificadas pelo back-end em cada operação. Ocultar um botão na interface não substitui a autorização no servidor.

| Ação | Responsável principal (criador) | Participante da tarefa |
| --- | --- | --- |
| Visualizar tarefa compartilhada | Sim | Sim |
| Alterar dados centrais da tarefa | Sim | Não |
| Gerenciar participantes | Sim | Não |
| Criar, alterar ou remover subtarefas | Sim | Não |
| Atualizar progresso de uma subtarefa atribuída | Sim, como administrador da tarefa | Sim, somente nas subtarefas atribuídas a si |
| Acessar tarefa individual de outra pessoa | Não | Não se aplica |

O que acontece com o acesso de alguém removido da tarefa, e se o responsável pode atualizar o progresso de qualquer subtarefa, são decisões que ainda precisam ser detalhadas.

## 5. Funcionalidades essenciais do MVP

O MVP deve validar o fluxo básico de organização e colaboração, sem tentar substituir plataformas acadêmicas completas.

1. **Conta e acesso:** cadastrar-se e entrar com uma conta de estudante.
2. **Tarefas:** criar, consultar e editar tarefas individuais e compartilhadas, incluindo ao menos título e prazo. A descrição pode ser incluída se estudantes confirmarem sua utilidade.
3. **Listagem:** ver tarefas próprias e tarefas compartilhadas das quais a pessoa participa, organizadas de forma que os próximos prazos sejam fáceis de encontrar.
4. **Colaboração:** o responsável principal pode adicionar e remover participantes de uma tarefa compartilhada.
5. **Subtarefas:** o responsável pode criar subtarefas, associar cada uma a uma pessoa participante e administrar sua organização.
6. **Progresso:** o responsável pode acompanhar as subtarefas; cada participante pode atualizar o progresso apenas das subtarefas atribuídas a si.
7. **Autorização:** impedir no back-end acessos e alterações incompatíveis com o papel da pessoa, inclusive quando uma requisição é enviada diretamente à API.
8. **Validação e erros:** validar dados obrigatórios e apresentar mensagens compreensíveis para dados inválidos, tarefa inexistente ou falta de permissão.

### Critério de sucesso inicial

Um pequeno grupo de estudantes consegue criar uma tarefa compartilhada, incluir colegas, dividir o trabalho em subtarefas, atribuí-las e acompanhar o progresso sem depender de uma conversa separada para descobrir quem é responsável pelo quê. A facilidade e a utilidade desse fluxo ainda precisam ser avaliadas com usuários.

## 6. Funcionalidades fora da primeira versão

Para manter o MVP pequeno, ficam fora do escopo inicial, salvo se a pesquisa mostrar que algum item é indispensável:

- Aplicativos nativos para celular e funcionamento sem conexão.
- Integração com calendários, ambientes virtuais de aprendizagem ou sistemas de instituições.
- Notificações por e-mail, push ou mensagens; regras de lembrete e notificações ainda não foram definidas.
- Chat, comentários, anexos e armazenamento de arquivos.
- Recorrência de tarefas, templates e criação automática a partir de disciplinas.
- Papéis de professor, turma institucional, notas e acompanhamento de desempenho acadêmico.
- Relatórios avançados de carga de trabalho, produtividade ou contribuição individual.
- Recomendações automáticas ou recursos de inteligência artificial.
- Workflows de aprovação, permissões configuráveis por tarefa ou outros controles avançados.

## 7. Requisitos funcionais

Os requisitos seguintes descrevem a proposta do MVP. Itens marcados como **a validar** dependem de decisão dos estudantes ou do responsável pelo produto.

| ID | Requisito |
| --- | --- |
| RF-01 | O sistema deve permitir que uma pessoa crie uma conta e autentique-se. **A validar:** quais dados de cadastro são necessários e se o acesso será por senha, provedor externo ou ambos. |
| RF-02 | Uma pessoa autenticada deve poder criar uma tarefa individual ou compartilhada. |
| RF-03 | Toda tarefa deve ter um responsável principal, definido como a pessoa que a criou. |
| RF-04 | O responsável principal deve poder consultar e alterar os dados centrais de suas tarefas. **A validar:** campos exatos, exclusão e possibilidade de arquivar tarefas concluídas. |
| RF-05 | Uma pessoa deve poder listar suas tarefas individuais e as tarefas compartilhadas às quais tem acesso. |
| RF-06 | O responsável principal deve poder incluir e remover participantes de uma tarefa compartilhada. **A validar:** como localizar colegas e se eles precisam aceitar um convite. |
| RF-07 | O responsável principal deve poder criar, editar e remover subtarefas de uma tarefa que administra. |
| RF-08 | Cada subtarefa pode ter um responsável escolhido entre as pessoas autorizadas na tarefa. **A validar:** se uma subtarefa pode ficar sem responsável e se o criador pode atribuí-la a si. |
| RF-09 | Uma pessoa participante deve poder consultar as tarefas compartilhadas às quais foi adicionada. |
| RF-10 | Uma pessoa participante deve poder atualizar o progresso somente das subtarefas atribuídas a ela. |
| RF-11 | O sistema deve negar no servidor qualquer leitura ou alteração feita por pessoa sem a permissão correspondente. |
| RF-12 | O sistema deve informar quando uma operação falhar por dados inválidos, recurso não encontrado ou permissão insuficiente, sem revelar dados de tarefas não autorizadas. |
| RF-13 | A listagem deve permitir identificar tarefas próximas do prazo. **A validar:** filtros, ordenação e estados que estudantes precisam ver. |

## 8. Requisitos não funcionais

| ID | Requisito |
| --- | --- |
| RNF-01 | **Segurança:** autenticação e autorização devem ser aplicadas no back-end; credenciais devem ser armazenadas usando mecanismos apropriados de proteção, nunca em texto puro. |
| RNF-02 | **Privacidade:** dados de tarefas devem ser acessíveis somente às pessoas autorizadas, seguindo o princípio de conceder o menor acesso necessário. |
| RNF-03 | **Integridade:** operações que alterem tarefa, participantes ou subtarefas devem preservar relacionamentos válidos e não deixar dados em estado inconsistente. |
| RNF-04 | **Usabilidade:** os fluxos principais — criar tarefa, dividir em subtarefas e atualizar progresso — devem ser compreensíveis para estudantes sem treinamento. |
| RNF-05 | **Compatibilidade:** a interface web deve funcionar nos navegadores modernos definidos para o projeto. **A validar:** suporte mínimo a celulares e navegadores específicos. |
| RNF-06 | **Manutenibilidade:** o back-end deve usar Java 21, Spring Boot e um monólito modular organizado por domínios, com separação clara entre controllers, services, repositories, entities e DTOs. |
| RNF-07 | **Persistência e evolução:** PostgreSQL será o banco previsto; mudanças de esquema devem ser versionadas com Flyway. |
| RNF-08 | **Qualidade:** regras importantes de negócio e autorização devem ter testes automatizados adequados, usando JUnit 5, Mockito e Spring Boot Test quando fizer sentido. |
| RNF-09 | **Configuração segura:** segredos e credenciais não devem ser incluídos no repositório; configurações de ambiente devem permitir executar a aplicação sem expor esses valores. |
| RNF-10 | **Desempenho e disponibilidade:** metas quantitativas ainda não foram definidas. Devem ser estabelecidas depois de estimar quantidade de usuários, volume de dados e ambiente de hospedagem. |
| RNF-11 | **Acessibilidade e idioma:** o idioma inicial e o nível de acessibilidade esperado ainda precisam ser definidos e avaliados com usuários. |

## 9. Decisões e requisitos a validar com estudantes

Estas perguntas devem orientar entrevistas curtas e testes de protótipo. As respostas podem mudar a priorização e o escopo do MVP.

### Problema e hábitos atuais

- Como estudantes registram hoje trabalhos, provas e prazos? Quais ferramentas usam juntos?
- Quando foi a última vez que perderam um prazo ou tiveram dificuldade de acompanhar uma atividade? O que aconteceu?
- O problema mais frequente é lembrar prazos, dividir trabalho, saber o andamento ou outro?
- Já usam uma ferramenta que resolve parte disso? O que funciona e o que falta?

### Tarefas e informações

- Quais tipos de atividade devem ser organizados: trabalhos, provas, leituras, apresentações ou outros?
- Além de título e prazo, quais dados são indispensáveis? Por exemplo, disciplina, descrição, link ou prioridade.
- Como estudantes entendem o progresso: estados como “a fazer / em andamento / concluída”, percentual ou outra forma?
- O estado da tarefa principal deve ser manual ou calculado a partir das subtarefas?
- Como devem funcionar tarefas atrasadas, concluídas, canceladas e excluídas?

### Grupos e responsabilidades

- Como identificar e adicionar colegas: nome de usuário, e-mail, link de convite ou código?
- Um convite precisa ser aceito antes de conceder acesso?
- Participantes podem convidar outras pessoas, ou somente quem criou a tarefa?
- O responsável principal deve poder editar o progresso de todas as subtarefas?
- O que acontece com subtarefas e autoria quando alguém sai ou é removido do grupo?
- Como estudantes percebem uma divisão de trabalho injusta? É necessário mostrar carga por participante no MVP?

### Uso e prioridade

- O acesso pelo celular é necessário desde a primeira versão?
- Lembretes de prazo são essenciais? Se sim, por qual canal e com quanta antecedência?
- É necessário vincular tarefas a disciplinas e semestres?
- Quais integrações, se alguma, são necessárias para que estudantes adotem o produto?
- Que informação ou resultado faria estudantes voltar a usar o sistema toda semana?

### Privacidade e conta

- Uma tarefa individual deve ser sempre privada? Há situações em que estudantes querem compartilhá-la depois?
- Quais dados pessoais são realmente necessários para criar uma conta?
- O que a pessoa espera que aconteça com seus dados quando encerra a conta?

## 10. Stack e arquitetura previstas

- **Back-end:** Java 21 e Spring Boot, com Spring Web para API REST, Spring Data JPA/Hibernate para persistência e Spring Security para autenticação e autorização.
- **Banco de dados:** PostgreSQL.
- **Migrações:** Flyway.
- **Build:** Maven.
- **Front-end:** HTML, CSS e JavaScript.
- **Versionamento:** Git e GitHub.
- **Arquitetura:** monólito modular, inicialmente organizado por domínios e com responsabilidades separadas entre controllers, services, repositories, entities e DTOs. A estrutura deve permanecer simples até haver uma necessidade concreta de maior complexidade.

## 11. Próximo passo recomendado

Validar primeiro o problema e o fluxo de colaboração com estudantes. Depois, revisar os requisitos marcados como “a validar”, definir os campos e estados mínimos de uma tarefa e transformar o escopo aprovado em etapas pequenas de implementação.
