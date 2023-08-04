## This file is a general .xdc for the Arty A7-35 Rev. D
## To use it in a project:
## - uncomment the lines corresponding to used pins
## - rename the used ports (in each line, after get_ports) according to the top level signal names in the project

## Clock signal
set_property -dict { PACKAGE_PIN E3    IOSTANDARD LVCMOS33 } [get_ports { CLK }]; #IO_L12P_T1_MRCC_35 Sch=gclk[100]

create_clock -add -name "sys_clk_pin" -period 10.0 [get_ports { CLK }];
set_clock_groups -name sys_clk_pin -physically_exclusive {*}${group_args}

## Pmod Header JD
set_property -dict { PACKAGE_PIN D4    IOSTANDARD LVCMOS33 } [get_ports { jtag_tdo }]; #IO_L11N_T1_SRCC_35 Sch=jd[1]
set_property -dict { PACKAGE_PIN F4    IOSTANDARD LVCMOS33 } [get_ports { jtag_tck }]; #IO_L13P_T2_MRCC_35 Sch=jd[3]
set_property -dict { PACKAGE_PIN E2    IOSTANDARD LVCMOS33 } [get_ports { jtag_tdi }]; #IO_L14P_T2_SRCC_35 Sch=jd[7]
set_property -dict { PACKAGE_PIN D2    IOSTANDARD LVCMOS33 } [get_ports { jtag_tms }]; #IO_L14N_T2_SRCC_35 Sch=jd[8]

## USB-UART Interface
set_property -dict { PACKAGE_PIN D10   IOSTANDARD LVCMOS33 } [get_ports { uart_txd }]; #IO_L19N_T3_VREF_16 Sch=uart_rxd_out
set_property -dict { PACKAGE_PIN A9    IOSTANDARD LVCMOS33 } [get_ports { uart_rxd }]; #IO_L14N_T2_SRCC_16 Sch=uart_txd_in

## Ethernet
set_property -dict {PACKAGE_PIN D17 IOSTANDARD LVCMOS33 IOB TRUE} [get_ports mii_RX_COL]
set_property -dict {PACKAGE_PIN G14 IOSTANDARD LVCMOS33 IOB TRUE} [get_ports mii_RX_CRS]
set_property -dict {PACKAGE_PIN F16 IOSTANDARD LVCMOS33 IOB TRUE} [get_ports mdio_C]
set_property -dict {PACKAGE_PIN K13 IOSTANDARD LVCMOS33 IOB TRUE} [get_ports mdio_IO]
set_property -dict {PACKAGE_PIN F15 IOSTANDARD LVCMOS33 IOB TRUE} [get_ports mii_RX_CLK]
set_property -dict {PACKAGE_PIN G16 IOSTANDARD LVCMOS33 IOB TRUE} [get_ports mii_RX_DV]
set_property -dict {PACKAGE_PIN D18 IOSTANDARD LVCMOS33 IOB TRUE} [get_ports {mii_RX_D[0]}]
set_property -dict {PACKAGE_PIN E17 IOSTANDARD LVCMOS33 IOB TRUE} [get_ports {mii_RX_D[1]}]
set_property -dict {PACKAGE_PIN E18 IOSTANDARD LVCMOS33 IOB TRUE} [get_ports {mii_RX_D[2]}]
set_property -dict {PACKAGE_PIN G17 IOSTANDARD LVCMOS33 IOB TRUE} [get_ports {mii_RX_D[3]}]
set_property -dict {PACKAGE_PIN C17 IOSTANDARD LVCMOS33 IOB TRUE} [get_ports mii_RX_ER]
set_property -dict {PACKAGE_PIN H16 IOSTANDARD LVCMOS33 IOB TRUE} [get_ports mii_TX_CLK]
set_property -dict {PACKAGE_PIN H15 IOSTANDARD LVCMOS33 IOB TRUE} [get_ports mii_TX_EN]
set_property -dict {PACKAGE_PIN H14 IOSTANDARD LVCMOS33 IOB TRUE} [get_ports {mii_TX_D[0]}]
set_property -dict {PACKAGE_PIN J14 IOSTANDARD LVCMOS33 IOB TRUE} [get_ports {mii_TX_D[1]}]
set_property -dict {PACKAGE_PIN J13 IOSTANDARD LVCMOS33 IOB TRUE} [get_ports {mii_TX_D[2]}]
set_property -dict {PACKAGE_PIN H17 IOSTANDARD LVCMOS33 IOB TRUE} [get_ports {mii_TX_D[3]}]

set_property -dict {PACKAGE_PIN G18 IOSTANDARD LVCMOS33 IOB TRUE} [get_ports ETH_REF_CLK]
set_property -dict {PACKAGE_PIN C16 IOSTANDARD LVCMOS33 IOB TRUE} [get_ports ETH_RSTN]

set_property -dict { PACKAGE_PIN C2    IOSTANDARD LVCMOS33 } [get_ports { RST }];
