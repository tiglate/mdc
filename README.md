# 📊 MDC - Market Data Collector

## 🌟 Overview
MDC (Market Data Collector) is an open-source application designed to download financial market data from public sources in Brazil. It fetches data from BACEN (Brazilian Central Bank), Anbima, and B3 (Brazilian Stock Exchange), processes it, and generates Excel spreadsheets in pre-defined directories.

## 🚀 Features
MDC currently supports the following jobs:

### 💰 BrazilianBondPrices
Downloads prices of Brazilian government bonds ("Títulos do Tesouro") issued by the Federal Government from Anbima.

### 📈 FinancialIndicators
Downloads various financial indicators from B3 (Brazilian Stock Exchange).

### 💱 Ptax
Downloads the dollar exchange rate in relation to the Brazilian Real from the Brazilian Central Bank (BACEN).

### 🔄 ExchangeRateParity
Downloads exchange rate parity data from the Brazilian Central Bank (BACEN).

### 📊 InterestRateCurves
Downloads interest rate curve data from Anbima, which is used for pricing and risk management.

### 📝 TradingAdjustments
Downloads trading adjustments data from B3 (Brazilian Stock Exchange).

### 💵 UpdatedNominalValues
Downloads updated nominal values of financial instruments from Anbima.

## 🛠️ Requirements
- Java 17 or higher
- Maven 3.6 or higher

## 📥 Installation

### Clone the repository
```bash
git clone https://github.com/tiglate/mdc.git
cd mdc
```

### Build the project
```bash
# Using Maven
mvn clean install

# Or using the Maven wrapper
./mvnw clean install  # For Linux/Mac
mvnw.cmd clean install  # For Windows
```

## ⚙️ Configuration
The application uses `application.properties` for configuration. Key settings include:

### Output Directories
```properties
brazilian-bond-prices.output-dir=C:/temp/mdc/
financial-indicators.output-dir=C:/temp/mdc/
ptax.output-dir=C:/temp/mdc/
exchange-rate-parity.output-dir=C:/temp/mdc/
interest-rate-curve.output-dir=C:/temp/mdc/
updated-nominal-values.output-dir=C:/temp/mdc/
trading-adjustments.output-dir=C:/temp/mdc/
```

### Data Source URLs
```properties
brazilian-bond-prices.download-base-url=https://www.anbima.com.br/informacoes/merc-sec/arqs/
financial-indicators.download-url=https://sistemaswebb3-derivativos.b3.com.br/financialIndicatorsProxy/FinancialIndicators/GetFinancialIndicators/eyJsYW5ndWFnZSI6InB0LWJyIn0=
ptax.download-url=https://olinda.bcb.gov.br/olinda/servico/PTAX/versao/v1/odata/CotacaoDolarPeriodo(dataInicial=@dataInicial,dataFinalCotacao=@dataFinalCotacao)?@dataInicial='%s'&@dataFinalCotacao='%s'&$top=100&$format=json&$select=cotacaoCompra,cotacaoVenda,dataHoraCotacao
exchange-rate-parity.download-url=https://www4.bcb.gov.br/Download/fechamento/
interest-rate-curve.download-url=https://www.anbima.com.br/informacoes/est-termo/CZ-down.asp
updated-nominal-values.download-url=https://www.anbima.com.br/informacoes/vna/vna-down.asp
trading-adjustments.download-url=https://www2.bmf.com.br/pages/portal/bmfbovespa/lumis/lum-ajustes-do-pregao-ptBR.asp
```

### HTTP Client Settings
```properties
file-downloader.http-client.connect-timeout-seconds=20
file-downloader.http-client.request-timeout-minutes=5
file-downloader.http-client.file-request-timeout-minutes=30
```

### Proxy Configuration (disabled by default)
```properties
file-downloader.http-client.proxy.enabled=false
#file-downloader.http-client.proxy.host=your-proxy.company.com
#file-downloader.http-client.proxy.port=8080
```

## 🚀 Usage

### Running a specific job
```bash
# Using Maven
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.batch.job.name=BrazilianBondPrices"
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.batch.job.name=FinancialIndicators"
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.batch.job.name=Ptax"
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.batch.job.name=ExchangeRateParity"
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.batch.job.name=InterestRateCurves"
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.batch.job.name=TradingAdjustments"
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.batch.job.name=UpdatedNominalValues"

# Using the JAR file
java -jar target/mdc-0.0.1-SNAPSHOT.jar --spring.batch.job.name=BrazilianBondPrices
java -jar target/mdc-0.0.1-SNAPSHOT.jar --spring.batch.job.name=FinancialIndicators
java -jar target/mdc-0.0.1-SNAPSHOT.jar --spring.batch.job.name=Ptax
java -jar target/mdc-0.0.1-SNAPSHOT.jar --spring.batch.job.name=ExchangeRateParity
java -jar target/mdc-0.0.1-SNAPSHOT.jar --spring.batch.job.name=InterestRateCurves
java -jar target/mdc-0.0.1-SNAPSHOT.jar --spring.batch.job.name=TradingAdjustments
java -jar target/mdc-0.0.1-SNAPSHOT.jar --spring.batch.job.name=UpdatedNominalValues
```

### Running with a specific reference date (for BrazilianBondPrices)
```bash
# Using Maven
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.batch.job.name=BrazilianBondPrices,referenceDate=2025-04-30"

# Using the JAR file
java -jar target/mdc-0.0.1-SNAPSHOT.jar --spring.batch.job.name=BrazilianBondPrices referenceDate=2025-04-30
```

## 📁 Output
After running a job, Excel files will be generated in the configured output directory:
`C:/temp/mdc/`

Each job will create its own Excel file in this directory.

## 🔄 Workflow
1. The application downloads data from the specified source
2. It parses the downloaded data (CSV or JSON)
3. It generates Excel spreadsheets with the processed data
4. The spreadsheets are saved to the configured output directory

## 📝 License
This project is licensed under the GNU General Public License v3.0 - see the [LICENSE.txt](LICENSE.txt) file for details.

## 🤝 Contributing
Contributions are welcome! Feel free to open issues or submit pull requests.
