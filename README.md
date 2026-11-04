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

## 10. Principais casos de uso

Os fluxos abaixo detalham como os perfis propostos usam o MVP. Quando uma regra ainda não foi definida, ela aparece como **proposta a validar** e não deve ser tratada como decisão fechada.

### Atores

- **Estudante:** pessoa com conta que usa a plataforma.
- **Responsável principal:** estudante que criou a tarefa e administra seus dados, participantes e subtarefas.
- **Participante:** estudante que aceitou ou recebeu acesso a uma tarefa compartilhada.
- **Estudante convidado:** pessoa que recebeu um convite para participar de uma tarefa.

Responsável principal, participante e estudante convidado são papéis que um mesmo estudante pode exercer em tarefas diferentes.

### UC-01 — Cadastrar conta

**Ator principal:** estudante não cadastrado.

**Pré-condição:** a pessoa ainda não possui uma conta com o identificador informado.

**Fluxo principal:**

1. A pessoa abre a tela de cadastro.
2. Informa os dados exigidos pelo formulário.
3. O sistema valida os dados e verifica se o identificador já está em uso.
4. O sistema cria a conta e informa que o cadastro foi concluído.

**Exceções:** dados obrigatórios ausentes ou inválidos; identificador já cadastrado; falha ao salvar a conta.

**A validar:** quais dados são necessários, se o cadastro usa e-mail e senha e se é necessária confirmação de e-mail.

### UC-02 — Autenticar-se

**Ator principal:** estudante cadastrado.

**Pré-condição:** a conta existe e está habilitada.

**Fluxo principal:**

1. A pessoa abre a tela de acesso e informa suas credenciais.
2. O sistema valida as credenciais.
3. O sistema inicia uma sessão autenticada e mostra as tarefas às quais a pessoa tem acesso.

**Exceções:** credenciais incorretas; conta inexistente ou indisponível; falha temporária de autenticação. A mensagem não deve revelar se um identificador específico possui conta.

**A validar:** método de autenticação e necessidade de recuperação de senha.

### UC-03 — Gerenciar disciplinas

**Ator principal:** estudante autenticado.

**Pré-condição:** a pessoa está autenticada.

**Fluxo principal:**

1. A pessoa abre sua lista de disciplinas.
2. Cria uma disciplina informando ao menos seu nome.
3. Pode consultar, editar ou remover disciplinas que administra.
4. Ao criar ou editar uma tarefa, pode associá-la a uma disciplina, se essa associação for confirmada para o MVP.

**Exceções:** nome ausente ou inválido; tentativa de alterar disciplina de outra pessoa; tentativa de remover uma disciplina que ainda esteja associada a tarefas — o comportamento nesse caso precisa ser definido.

**Proposta a validar:** cada estudante mantém sua própria lista de disciplinas. Também falta definir campos adicionais, como período ou instituição, e como a disciplina aparece para participantes de uma tarefa compartilhada.

### UC-04 — Criar tarefa individual

**Ator principal:** estudante autenticado, que será o responsável principal.

**Pré-condição:** a pessoa está autenticada; a disciplina pode ser associada se já existir e se essa relação fizer parte do MVP.

**Fluxo principal:**

1. A pessoa escolhe criar uma tarefa individual.
2. Informa título e prazo, além de outros campos que venham a ser definidos.
3. Opcionalmente associa uma disciplina.
4. Salva a tarefa.
5. O sistema registra a pessoa como responsável principal e disponibiliza a tarefa somente a ela.

**Exceções:** título ou prazo ausente ou inválido; disciplina inexistente ou à qual a pessoa não tem acesso; falha ao salvar.

### UC-05 — Criar tarefa compartilhada

**Ator principal:** estudante autenticado, que será o responsável principal.

**Pré-condição:** a pessoa está autenticada.

**Fluxo principal:**

1. A pessoa escolhe criar uma tarefa compartilhada.
2. Informa título e prazo e, se aplicável, associa uma disciplina.
3. Salva a tarefa.
4. O sistema registra a pessoa criadora como responsável principal.
5. A tarefa fica visível ao criador; outras pessoas passam a vê-la quando receberem acesso conforme o fluxo de convite.

**Exceções:** dados obrigatórios ausentes ou inválidos; disciplina sem acesso; falha ao salvar. Se a pessoa não tiver permissão para administrar a tarefa, o sistema rejeita a operação.

### UC-06 — Convidar participante

**Atores:** responsável principal e estudante convidado.

**Pré-condição:** existe uma tarefa compartilhada que o responsável principal pode administrar.

**Fluxo principal proposto:**

1. O responsável abre a tarefa e escolhe convidar alguém.
2. Informa um identificador de uma conta existente, como o e-mail ou nome de usuário.
3. O sistema cria um convite pendente.
4. O estudante convidado consulta os convites pendentes dentro da plataforma e aceita.
5. O sistema registra a participação e permite que o novo participante consulte a tarefa.

**Exceções:** conta não encontrada; pessoa já participante; convite já pendente; convite recusado; tentativa de convidar feita por alguém que não é o responsável principal; tarefa não compartilhada.

**A validar:** identificador para localizar colegas, aceite obrigatório, validade do convite e se participantes podem convidar outras pessoas. O fluxo acima é uma proposta de MVP, não uma regra confirmada.

### UC-07 — Gerenciar subtarefas

**Ator principal:** responsável principal.

**Pré-condição:** existe uma tarefa que a pessoa pode administrar; para atribuir uma subtarefa, o responsável escolhido tem acesso à tarefa.

**Fluxo principal:**

1. O responsável abre a tarefa.
2. Cria uma subtarefa com as informações necessárias.
3. Opcionalmente atribui a subtarefa a si ou a um participante autorizado.
4. Pode editar, reatribuir ou remover subtarefas enquanto administra a tarefa.
5. O sistema atualiza a visão da tarefa para as pessoas autorizadas.

**Exceções:** tentativa de gerenciamento por participante sem permissão; tarefa inexistente ou inacessível; pessoa escolhida não pertence à tarefa; dados inválidos.

### UC-08 — Atualizar progresso de subtarefa

**Ator principal:** participante a quem a subtarefa foi atribuída.

**Pré-condição:** a pessoa está autenticada, tem acesso à tarefa e é a responsável pela subtarefa.

**Fluxo principal:**

1. A pessoa abre uma tarefa compartilhada e seleciona uma subtarefa atribuída a ela.
2. Atualiza seu progresso.
3. O sistema valida a permissão e salva a alteração.
4. O responsável principal e os demais participantes autorizados podem consultar o progresso atualizado.

**Exceções:** subtarefa não atribuída à pessoa; tarefa ou subtarefa removida; pessoa sem acesso à tarefa; estado de progresso inválido; falha ao salvar.

**A validar:** quais estados de progresso serão usados. A matriz de acesso deste documento propõe que o responsável principal também possa atualizar qualquer subtarefa como administrador; falta confirmar se estudantes esperam essa possibilidade e como distinguir uma atualização feita pelo responsável principal daquela feita pela pessoa atribuída.

### UC-09 — Consultar tarefas e prazos

**Ator principal:** estudante autenticado.

**Pré-condição:** a pessoa está autenticada.

**Fluxo principal:**

1. A pessoa abre sua lista de tarefas.
2. O sistema apresenta suas tarefas individuais e as tarefas compartilhadas às quais ela tem acesso, com os respectivos prazos.
3. A pessoa abre uma tarefa para consultar seus dados e subtarefas, conforme suas permissões.
4. A pessoa identifica tarefas próximas do prazo; ordenação e filtros podem ser oferecidos conforme o escopo confirmado.

**Exceções:** nenhuma tarefa cadastrada — o sistema apresenta uma lista vazia com orientação para criar a primeira; tarefa inexistente ou sem permissão — o sistema não revela seus dados; falha ao consultar os dados.

**A validar:** filtros necessários, ordenação padrão e como destacar tarefas atrasadas ou concluídas.

## 11. Stack e arquitetura previstas

- **Back-end:** Java 21 e Spring Boot, com Spring Web para API REST, Spring Data JPA/Hibernate para persistência e Spring Security para autenticação e autorização.
- **Banco de dados:** PostgreSQL.
- **Migrações:** Flyway.
- **Build:** Maven.
- **Front-end:** HTML, CSS e JavaScript.
- **Versionamento:** Git e GitHub.
- **Arquitetura:** monólito modular, inicialmente organizado por domínios e com responsabilidades separadas entre controllers, services, repositories, entities e DTOs. A estrutura deve permanecer simples até haver uma necessidade concreta de maior complexidade.

## 12. Próximo passo recomendado

Validar primeiro o problema e o fluxo de colaboração com estudantes. Depois, revisar os requisitos marcados como “a validar”, definir os campos e estados mínimos de uma tarefa e transformar o escopo aprovado em etapas pequenas de implementação.
