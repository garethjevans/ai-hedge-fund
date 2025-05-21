# ai-hedge-fund

An implementation of the ai-hedge-fund in Spring-AI/Tanzu AI Server.

This is still a huge work in progress, there are lots of missing parts but i'll add them slowly.

## Running

You'll need to get an API key from https://financialdatasets.ai, with a bit of credit.  After its run once, the data should be cached so you won't need too much credit.

```shell
export FINANCIAL_DATASETS_API_KEY=<your-key>

./mvn -pl agent-warren-buffett spring-boot:run
```

This will launch an MCP server on localhost:10900.

To run the tests, which will invoke the MCP server tests:

```shell
export FINANCIAL_DATASETS_API_KEY=<your-key>

./mvn -pl agent-warren-buffett test
```
