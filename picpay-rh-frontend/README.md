# PicPay RH — Front-end (Vue.js 3 + Vuetify)

Interface web do desafio **Sistema de Contratação de Funcionários do PicPay**.
Consome a API REST Spring Boot do projeto `Picpay-RH` (pasta vizinha).

```
Front-end (Vue 3) → HTTP (axios) → Spring Boot REST API → ArrayList
```

## Como rodar

**1. Suba a API primeiro** (na pasta `Picpay-RH`):

```sh
./mvnw spring-boot:run
```

A API sobe em `http://localhost:8080`.

**2. Suba o front-end** (nesta pasta):

```sh
npm install
npm run dev
```

A interface abre em `http://localhost:5173`.

A URL da API fica na variável `VITE_API_URL` do arquivo `.env` — mude ali se
usar outra porta.

### Outros comandos

```sh
npm run type-check   # checagem de tipos (vue-tsc)
npm run build        # type-check + build de produção em dist/
npm run preview      # serve o build de produção
```

## Telas

| Rota | Tela | O que faz |
|---|---|---|
| `/` | Home | Apresenta o sistema e o mapa de endpoints |
| `/funcionario` | Listagem | Lista, pesquisa, consulta por ID e exclui |
| `/funcionario/:id` | Cadastro/Edição | `id = 0` cadastra (POST); demais editam (PUT ou PATCH) |
| `/indicadores` | Indicadores | Totais por status do processo seletivo |

## Estrutura

```
src/
├── services/api.ts          ← toda a comunicação HTTP com o Spring Boot
├── types/Funcionario.ts     ← espelho da classe Funcionario do back-end
├── router/index.ts          ← rotas da SPA
└── views/
    ├── Home.vue
    ├── 404.vue
    └── Funcionario/
        ├── FuncionarioList.vue
        ├── FuncionarioEdit.vue
        └── FuncionarioIndicadores.vue
```

---

## Apresentação — justificativa da escolha

**1. Qual tecnologia Front-end você escolheu?**
Vue.js 3 (Composition API + TypeScript), com Vuetify para os componentes
visuais e axios para as requisições HTTP.

**2. Por que escolheu essa tecnologia?**
Vue tem sintaxe próxima do HTML puro, o que deixa a leitura do template
simples, e é reativo: a tela se atualiza sozinha quando os dados mudam. Como o
sistema é um CRUD com várias telas, a organização em componentes e o
roteamento (SPA) evitam recarregar a página a cada ação. Vuetify entrega
tabela, diálogos e formulários prontos, permitindo focar na integração com a
API — que é o objetivo do desafio.

**3. Como o Front-end se comunica com o Spring Boot?**
Por requisições HTTP feitas com axios, centralizadas em
[`src/services/api.ts`](src/services/api.ts). Lá existe uma instância do axios
com a `baseURL` apontando para `http://localhost:8080`. Como front e back
rodam em portas diferentes, o back-end libera o acesso com `@CrossOrigin` no
controller.

**4. Como realiza uma requisição GET?**

```ts
// Todos, com os filtros da pesquisa como query params
api.get<Funcionario[]>("/funcionarios", { params: { nome, cargo, status } });

// Por ID
api.get<Funcionario>(`/funcionarios/${id}`);
```

A busca por nome/cargo/status é resolvida **no back-end**: os campos do
formulário viram `?nome=...&cargo=...&status=...`. A tela mostra a última URL
chamada logo abaixo dos filtros.

**5. Como realiza um POST?**

```ts
api.post<Funcionario>("/funcionarios", funcionario);
```

Enviado ao salvar um funcionário novo (rota `/funcionario/0`). O corpo é o
objeto do formulário em JSON; o back-end gera o `id` e devolve o registro
criado com status 201.

**6. Como realiza um PUT?**

```ts
api.put<Funcionario>(`/funcionarios/${id}`, funcionario);
```

Envia **todos** os campos do formulário, substituindo o registro inteiro.

**7. Como realiza um PATCH?**

```ts
api.patch<Funcionario>(`/funcionarios/${id}`, camposAlterados);
```

Na tela de edição existe um botão **"Usar atualização parcial (PATCH)"**.
Com ele ligado, o sistema compara o formulário com os dados originalmente
carregados e envia **apenas os campos alterados** — os nomes desses campos
aparecem na tela antes de salvar, deixando visível a diferença para o PUT.

**8. Como realiza um DELETE?**

```ts
api.delete(`/funcionarios/${id}`);
```

Disparado pelo botão da lixeira na listagem, após confirmação em um diálogo.
A mensagem devolvida pelo back-end é exibida em um toast.

**9. Quais foram as principais dificuldades encontradas?**
- Comunicação entre aplicações separadas: sem o CORS liberado no Spring Boot,
  o navegador bloqueia as chamadas (o `curl` funciona, mas a tela não).
- Diferenciar PUT de PATCH na prática: foi preciso guardar uma cópia dos dados
  originais para calcular quais campos realmente mudaram.
- Tratar os erros da API (400 e 404) e traduzi-los em mensagens úteis, em vez
  de deixar a tela quebrar em silêncio.
- Tipos: o campo salário chega do input como texto e precisa virar número
  antes de ir para o JSON.

**10. Quais são as vantagens e limitações da tecnologia escolhida?**

*Vantagens:* reatividade (a tabela atualiza sozinha após salvar/excluir);
componentização e reaproveitamento; navegação SPA sem recarregar a página;
o Vuetify dá um resultado visual profissional rapidamente.

*Limitações:* exige um projeto separado do back-end (dois processos rodando);
depende de CORS configurado; tem curva de aprendizado maior que Thymeleaf ou
HTML puro; e é mais estrutura do que um CRUD pequeno realmente precisaria.
