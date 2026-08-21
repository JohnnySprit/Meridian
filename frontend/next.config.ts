import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  async rewrites() {
    return [
      {
        source: "/oauth/:path*",
        destination: "http://localhost:8080/oauth/:path*",
      },
      {
        source: "/portfolio",
        destination: "http://localhost:8080/portfolio",
      },
    ];
  },
};

export default nextConfig;
